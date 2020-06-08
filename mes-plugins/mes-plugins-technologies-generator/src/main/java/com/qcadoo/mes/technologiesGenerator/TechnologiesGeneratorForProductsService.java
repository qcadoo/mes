package com.qcadoo.mes.technologiesGenerator;

import com.google.common.collect.Lists;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.technologies.TechnologyNameAndNumberGenerator;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.dataProvider.TechnologyDataProvider;
import com.qcadoo.mes.technologies.domain.TechnologyId;
import com.qcadoo.mes.technologiesGenerator.constants.GeneratorContextFields;
import com.qcadoo.mes.technologiesGenerator.constants.GeneratorTreeNodeFields;
import com.qcadoo.mes.technologiesGenerator.constants.ProductFieldsTG;
import com.qcadoo.mes.technologiesGenerator.constants.TechnologiesGeneratorConstants;
import com.qcadoo.mes.technologiesGenerator.customization.product.ProductCustomizer;
import com.qcadoo.mes.technologiesGenerator.customization.product.domain.ProductSuffixes;
import com.qcadoo.mes.technologiesGenerator.customization.technology.TechnologyCustomizer;
import com.qcadoo.mes.technologiesGenerator.customization.technology.TechnologyProductsCustomizer;
import com.qcadoo.mes.technologiesGenerator.dataProvider.TechnologyStructureNodeDataProvider;
import com.qcadoo.mes.technologiesGenerator.dataProvider.TechnologyStructureTreeDataProvider;
import com.qcadoo.mes.technologiesGenerator.domain.TechnologyStructureNodeType;
import com.qcadoo.mes.technologiesGenerator.view.GeneratorView;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Service
public class TechnologiesGeneratorForProductsService {

    private static final Logger LOG = LoggerFactory.getLogger(TechnologiesGeneratorForProductsService.class);

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TechnologyCustomizer technologyCustomizer;

    @Autowired
    private TechnologyStructureNodeDataProvider nodeDataProvider;

    @Autowired
    private TechnologyStructureTreeDataProvider technologyStructureTreeDataProvider;

    @Autowired
    private ProductCustomizer productCustomizer;

    @Autowired
    private TechnologyNameAndNumberGenerator technologyNameAndNumberGenerator;

    @Autowired
    private TechnologyDataProvider technologyDataProvider;

    @Autowired
    private TechnologyProductsCustomizer technologyOperationProductsCustomizer;

    @Autowired
    private ParameterService parameterService;

    @Async
    public void performGeneration(final GeneratorView generatorView) {
        LOG.info(String.format("Start generation technologies for products. Generator name : %S", generatorView.getFormEntity()
                .getStringField(GeneratorContextFields.NAME)));

        Entity context = generatorView.getFormEntity();
        List<Entity> products = context.getHasManyField(GeneratorContextFields.PRODUCTS);
        try {
            Optional<List<Entity>> optionalNodes = nodeDataProvider.getCastumizedNodesForContext(context);
            if (optionalNodes.isPresent() && !optionalNodes.get().isEmpty()) {
                final Entity finalContext = context;
                products.forEach(pr -> performGenerationTechnologyForProduct(pr, optionalNodes.get(), generatorView, finalContext));
            }
        } catch (Exception ex) {
            LOG.warn("An error occurred while generating technology", ex);
        } finally {
            afterGenerationComplete(generatorView, context);
        }
        LOG.info(String.format("Finish generation technologies for products. Generator name : %S", generatorView.getFormEntity()
                .getStringField(GeneratorContextFields.NAME)));
    }

    private void afterGenerationComplete(GeneratorView generatorView, Entity context) {
        List<Entity> products = Lists.newArrayList();
        context = context.getDataDefinition().get(generatorView.getFormEntity().getId());
        context.setField(GeneratorContextFields.GENERATION_IN_PROGRSS, false);
        context.setField(GeneratorContextFields.PRODUCTS, products);
        context = context.getDataDefinition().save(context);
    }

    private void performGenerationTechnologyForProduct(final Entity product, final List<Entity> nodes,
            final GeneratorView generatorView, final Entity context) {
        preformForProduct(product, nodes, generatorView, context);
    }

    private void preformForProduct(Entity product, List<Entity> nodes, GeneratorView generatorView, Entity context) {
        nodes.forEach(node -> customizeTechnologyForNode(node, product, generatorView, context));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void customizeTechnologyForNode(Entity node, Entity product, GeneratorView generatorView, final Entity context) {
        generateTechnologyNode(node, product, context);
    }

    private void generateTechnologyNode(Entity node, Entity product, Entity context) {
        try {
            Optional<Either<String, TechnologyId>> customizedTechId = Optional.ofNullable(product).flatMap(
                    mainProduct -> Optional.ofNullable(node.getId()).flatMap(
                            nodeId -> customize(nodeId, mainProduct,
                                    GeneratorSettings.from(context, parameterService.getParameter()), true)));
            customizedTechId.ifPresent(cti -> {
                if (cti.isRight()) {
                    TechnologyId technologyId = cti.getRight();
                    technologyCustomizer.addCustomizedProductToQualityCard(technologyId);
                }
            });
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Cannot perform technology customization due to unexpected error", e);
            }

        }
    }

    public Optional<Either<String, TechnologyId>> customize(final Long nodeId, final Entity mainProduct,
            final GeneratorSettings settings, boolean generationMode) {
        Optional<Entity> maybeNode = technologyStructureTreeDataProvider.tryFind(nodeId);
        if (!maybeNode.isPresent()) {
            return Optional.of(Either.left(String.format("Cannot find generator node with id = '%s'", nodeId)));
        }
        Optional<Either<String, TechnologyId>> customizationResult = maybeNode.filter(this::isCustomizableNode).map(
                node -> customize(node, mainProduct, settings, generationMode));
        if (customizationResult.map(Either::isLeft).orElse(false)) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return customizationResult;
    }

    private boolean isCustomizableNode(final Entity nodeEntity) {
        TechnologyStructureNodeType type = TechnologyStructureNodeType.of(nodeEntity);
        return type == TechnologyStructureNodeType.COMPONENT || type == TechnologyStructureNodeType.CUSTOMIZED_COMPONENT;
    }

    private Either<String, TechnologyId> customize(final Entity node, final Entity mainProduct, final GeneratorSettings settings,
            boolean generationMode) {
        Entity productTechnology = node.getBelongsToField(GeneratorTreeNodeFields.PRODUCT_TECHNOLOGY);
        if (productTechnology == null) {
            return Either.left("Cannot find technology for product");
        }
        if (TechnologyStructureNodeType.of(node) == TechnologyStructureNodeType.CUSTOMIZED_COMPONENT && !generationMode) {
            return Either.right(new TechnologyId(productTechnology.getId()));
        }
        return copy(productTechnology).flatMap(t -> customize(node, mainProduct, settings, t, generationMode));
    }

    private Either<String, TechnologyId> customize(final Entity node, final Entity mainProduct, final GeneratorSettings settings,
            final Entity technology, boolean generationMode) {
        TechnologyId technologyId = new TechnologyId(technology.getId());
        if (settings.shouldCreateAndSwitchProducts()) {
            Entity newProduct = resolveProduct(node, mainProduct, settings);
            return technologyOperationProductsCustomizer
                    .customizeForTechnologyGeneration(technologyId, mainProduct, newProduct, settings)
                    .flatMap(techId -> setupTechnologyNumberAndName(settings, techId, newProduct))
                    .flatMap(techId -> save(node, techId, Optional.of(mainProduct)));
        } else if (node.getBelongsToField(GeneratorTreeNodeFields.PARENT) == null) {
            return technologyOperationProductsCustomizer.prepareMainTechnologyProduct(technology, mainProduct)
                    .map(x -> technologyId).flatMap(techId -> setupTechnologyNumberAndName(settings, techId, mainProduct))
                    .flatMap(techId -> save(node, techId, Optional.of(mainProduct)));
        } else {
            Entity nodeProduct = node.getBelongsToField(GeneratorTreeNodeFields.PRODUCT);
            return setupTechnologyNumberAndName(settings, technology, nodeProduct).flatMap(
                    (techId) -> save(node, techId, Optional.empty()));
        }
    }

    private Either<String, Entity> copy(final Entity technology) {
        DataDefinition technologyDD = technology.getDataDefinition();
        Entity copy = technologyDD.copy(technology.getId()).get(0);
        if (copy.isValid()) {
            return Either.right(copy);
        }
        return Either.left("Cannot copy technology due to validation errors.");
    }

    private Either<String, TechnologyId> setupTechnologyNumberAndName(final GeneratorSettings settings,
            final TechnologyId technologyId, final Entity product) {
        // I'm aware that loading & mapping the whole technology isn't cheap, but it will be far more easier to maintain if we
        // won't be depending on the operations' order. Trust me, avoiding issues with unwanted overriding some data by further
        // cascade saving is worth of it. Really.
        Optional<Either<String, TechnologyId>> setupResults = technologyDataProvider.tryFind(technologyId.get()).map(
                technology -> setupTechnologyNumberAndName(settings, technology, product));
        return setupResults
                .orElseGet(() -> Either.left(String.format("Cannot find technology with id = '%s'", technologyId.get())));
    }

    private Either<String, TechnologyId> setupTechnologyNumberAndName(final GeneratorSettings settings, final Entity technology,
            final Entity product) {
        technology.setField("generatorContext", settings.getGenerationContext());
        technology.setField(TechnologyFields.NUMBER, technologyNameAndNumberGenerator.generateNumber(product));
        technology.setField(TechnologyFields.NAME, technologyNameAndNumberGenerator.generateName(product));
        Entity savedTech = technology.getDataDefinition().save(technology);
        if (savedTech.isValid()) {
            LOG.info(String.format("Generated technology with number : %S", technology.getStringField(TechnologyFields.NUMBER)));
            return Either.right(new TechnologyId(savedTech.getId()));
        } else {
            return Either.left("Cannot setup technology name and number due to validation errors");
        }
    }

    private Either<String, TechnologyId> save(final Entity node, final TechnologyId technologyId, final Optional<Entity> product) {
        Entity tech = dataDefinitionService.get(TechnologiesGeneratorConstants.PLUGIN_IDENTIFIER,
                "generatorTechnologiesForProduct").create();
        product.ifPresent(p -> tech.setField("product", p));
        tech.setField("technology", technologyId.get());
        tech.setField("generatorTreeNode", node);
        tech.setField("generatorContext", node.getBelongsToField("generatorContext"));
        tech.getDataDefinition().save(tech);
        return Either.right(technologyId);

    }

    private Entity resolveProduct(final Entity node, final Entity mainProduct, final GeneratorSettings settings) {
        if (node.getBelongsToField(GeneratorTreeNodeFields.PARENT) == null) {
            return mainProduct;
        }
        Entity product = node.getBelongsToField(GeneratorTreeNodeFields.PRODUCT);
        if (product.getBooleanField(ProductFieldsTG.FROM_GENERATOR)) {
            return productCustomizer.findOrCreate(product.getBelongsToField(ProductFields.PARENT), mainProduct,
                    ProductSuffixes.from(mainProduct), settings);
        }
        return product;
    }
}
