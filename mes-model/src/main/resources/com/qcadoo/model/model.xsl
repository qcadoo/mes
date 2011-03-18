<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:qcd="http://www.qcadoo.com/model"
	exclude-result-prefixes="qcd">

	<xsl:output method="xml" version="1.0" encoding="UTF-8"
		doctype-system="http://www.hibernate.org/dtd/hibernate-mapping-3.0.dtd"
		doctype-public="-//Hibernate/Hibernate Mapping DTD 3.0//EN" />

	<xsl:variable name="smallcase" select="'abcdefghijklmnopqrstuvwxyz'" />
	<xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />

	<xsl:template name="entityName">
		<xsl:param name="modelName" />
		<xsl:param name="pluginName" />
		<xsl:attribute name="class">
			<xsl:value-of select="concat('com.qcadoo.model.beans.', $pluginName, '.', translate(substring($pluginName, 1, 1),  $smallcase, $uppercase), substring($pluginName, 2), translate(substring($modelName, 1, 1),  $smallcase, $uppercase), substring($modelName, 2))" />
		</xsl:attribute>
	</xsl:template>

	<xsl:template match="//qcd:model">
		<hibernate-mapping>
			<class>
				<xsl:variable name="table_name">
					<xsl:value-of select="concat(/qcd:model/@plugin, '_', @name)" />
				</xsl:variable>
				<xsl:attribute name="table">
				    <xsl:value-of select="$table_name" />
				</xsl:attribute>
				<xsl:attribute name="name">
					<xsl:value-of select="concat('com.qcadoo.model.beans.', @plugin, '.', translate(substring(@plugin, 1, 1),  $smallcase, $uppercase), substring(@plugin, 2), translate(substring(@name, 1, 1),  $smallcase, $uppercase), substring(@name, 2))" />
				</xsl:attribute>
				<id column="id" name="id" type="long">
					<generator class="increment" />
				</id>
				<xsl:apply-templates />
				<xsl:if test="@insertable='false'">
					<sql-insert>
						<xsl:value-of
							select="concat('insert must not be executed on ', $table_name)" />
					</sql-insert>
				</xsl:if>
				<xsl:if test="@updatable='false'">
					<sql-update>
						<xsl:value-of
							select="concat('update must not be executed on ', $table_name)" />
					</sql-update>
				</xsl:if>
				<xsl:if test="@deletable='false'">
					<sql-delete>
						<xsl:value-of
							select="concat('delete must not be executed on ', $table_name)" />
					</sql-delete>
				</xsl:if>
			</class>
		</hibernate-mapping>
	</xsl:template>

	<xsl:template name="property">
		<xsl:attribute name="name">
			    <xsl:value-of select="@name" />
			</xsl:attribute>
		<xsl:attribute name="not-null">
				<xsl:choose>
					<xsl:when test="@required='true'">
						<xsl:text>true</xsl:text>						
					</xsl:when>
					<xsl:when test="local-name()='priority'">
						<xsl:text>true</xsl:text>						
					</xsl:when>
					<xsl:otherwise>
						<xsl:text>false</xsl:text>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:attribute>
		<xsl:if test="@unique='true'">
			<xsl:attribute name="unique">true</xsl:attribute>
		</xsl:if>
		<xsl:choose>
			<xsl:when test="./qcd:validatesLength[@is]">
				<xsl:attribute name="length">
						<xsl:value-of select="./qcd:validatesLength/@is" />	
					</xsl:attribute>
			</xsl:when>
			<xsl:when test="./qcd:validatesLength[@max]">
				<xsl:attribute name="length">
						<xsl:value-of select="./qcd:validatesLength/@max" />	
					</xsl:attribute>
			</xsl:when>
		</xsl:choose>
		<xsl:choose>
			<xsl:when test="./qcd:validatesPrecision[@is]">
				<xsl:attribute name="precision">
						<xsl:value-of select="./qcd:validatesPrecision/@is" />	
					</xsl:attribute>
			</xsl:when>
			<xsl:when test="./qcd:validatesPrecision[@max]">
				<xsl:attribute name="precision">
						<xsl:value-of select="./qcd:validatesPrecision/@max" />	
					</xsl:attribute>
			</xsl:when>
		</xsl:choose>
		<xsl:choose>
			<xsl:when test="./qcd:validatesScale[@is]">
				<xsl:attribute name="scale">
						<xsl:value-of select="./qcd:validatesScale/@is" />	
					</xsl:attribute>
			</xsl:when>
			<xsl:when test="./qcd:validatesScale[@max]">
				<xsl:attribute name="scale">
						<xsl:value-of select="./qcd:validatesScale/@max" />	
					</xsl:attribute>
			</xsl:when>
		</xsl:choose>
		<column>
			<xsl:attribute name="name">
				<xsl:choose>
				<xsl:when test="local-name()='belongsTo'">
			    	<xsl:value-of select="concat(@name, '_id')" />
			    </xsl:when>
			    <xsl:otherwise>
			    	<xsl:value-of select="@name" />
			    </xsl:otherwise>
			    </xsl:choose>
			</xsl:attribute>
			<xsl:if test="@default">
				<xsl:attribute name="default">
				    	<xsl:value-of select="concat(&quot;'&quot;, @default, &quot;'&quot;)" />
					</xsl:attribute>
			</xsl:if>
		</column>
	</xsl:template>

	<xsl:template match="//qcd:model/qcd:fields/qcd:integer[not(@persistent='false')]">
		<property>
			<xsl:attribute name="type">integer</xsl:attribute>
			<xsl:call-template name="property" />
		</property>
	</xsl:template>

	<xsl:template match="//qcd:model/qcd:fields/qcd:priority">
		<property>
			<xsl:attribute name="type">integer</xsl:attribute>
			<xsl:call-template name="property" />
		</property>
	</xsl:template>

	<xsl:template match="//qcd:model/qcd:fields/qcd:string[not(@expression) and not(@persistent='false')]">
		<property>
			<xsl:attribute name="type">string</xsl:attribute>
			<xsl:call-template name="property" />
		</property>
	</xsl:template>

	<xsl:template match="//qcd:model/qcd:fields/qcd:password[not(@persistent='false')]">
		<property>
			<xsl:attribute name="type">string</xsl:attribute>
			<xsl:call-template name="property" />
		</property>
	</xsl:template>

	<xsl:template match="//qcd:model/qcd:fields/qcd:text[not(@persistent='false')]">
		<property>
			<xsl:attribute name="type">text</xsl:attribute>
			<xsl:call-template name="property" />
		</property>
	</xsl:template>

	<xsl:template match="//qcd:model/qcd:fields/qcd:decimal[not(@persistent='false')]">
		<property>
			<xsl:attribute name="type">big_decimal</xsl:attribute>
			<xsl:call-template name="property" />
		</property>
	</xsl:template>

	<xsl:template match="//qcd:model/qcd:fields/qcd:datetime[not(@persistent='false')]">
		<property>
			<xsl:attribute name="type">timestamp</xsl:attribute>
			<xsl:call-template name="property" />
		</property>
	</xsl:template>

	<xsl:template match="//qcd:model/qcd:fields/qcd:date[not(@persistent='false')]">
		<property>
			<xsl:attribute name="type">date</xsl:attribute>
			<xsl:call-template name="property" />
		</property>
	</xsl:template>

	<xsl:template match="//qcd:model/qcd:fields/qcd:boolean[not(@persistent='false')]">
		<property>
			<xsl:attribute name="type">boolean</xsl:attribute>
			<xsl:call-template name="property" />
		</property>
	</xsl:template>

	<xsl:template match="//qcd:model/qcd:fields/qcd:enum[not(@persistent='false')]">
		<property>
			<xsl:attribute name="type">string</xsl:attribute>
			<xsl:call-template name="property" />
		</property>
	</xsl:template>

	<xsl:template match="//qcd:model/qcd:fields/qcd:dictionary[not(@persistent='false')]">
		<property>
			<xsl:attribute name="type">string</xsl:attribute>
			<xsl:call-template name="property" />
		</property>
	</xsl:template>

	<xsl:template match="//qcd:model/qcd:fields/qcd:long[not(@persistent='false')]">
		<property>
			<xsl:attribute name="type">long</xsl:attribute>
			<xsl:call-template name="property" />
		</property>
	</xsl:template>

	<xsl:template match="//qcd:model/qcd:fields/qcd:short[not(@persistent='false')]">
		<property>
			<xsl:attribute name="type">short</xsl:attribute>
			<xsl:call-template name="property" />
		</property>
	</xsl:template>

	<xsl:template match="//qcd:model/qcd:fields/qcd:float[not(@persistent='false')]">
		<property>
			<xsl:attribute name="type">float</xsl:attribute>
			<xsl:call-template name="property" />
		</property>
	</xsl:template>

	<xsl:template match="//qcd:model/qcd:fields/qcd:double[not(@persistent='false')]">
		<property>
			<xsl:attribute name="type">double</xsl:attribute>
			<xsl:call-template name="property" />
		</property>
	</xsl:template>

	<xsl:template match="//qcd:model/qcd:fields/qcd:hasMany[not(@persistent='false')] | //qcd:model/qcd:fields/qcd:tree[not(@persistent='false')]">
		<set>
			<xsl:attribute name="cascade">
			<xsl:choose>
				<xsl:when test="@cascade='delete'">delete</xsl:when>
				<xsl:otherwise>none</xsl:otherwise>
			</xsl:choose>
		</xsl:attribute>
			<xsl:attribute name="lazy">true</xsl:attribute>
			<xsl:attribute name="name">
			    <xsl:value-of select="@name" />
			</xsl:attribute>
			<key>
				<xsl:attribute name="column">
            	<xsl:value-of select="concat(@joinField, '_id')" />
            </xsl:attribute>
			</key>
			<one-to-many>
				<xsl:choose>
					<xsl:when test="@plugin">
						<xsl:call-template name="entityName">
							<xsl:with-param name="modelName" select="@model" />
							<xsl:with-param name="pluginName" select="@plugin" />
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="entityName">
							<xsl:with-param name="modelName" select="@model" />
							<xsl:with-param name="pluginName" select="/qcd:model/@plugin" />
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
			</one-to-many>
		</set>
	</xsl:template>

	<xsl:template match="//qcd:model/qcd:fields/qcd:belongsTo[not(@persistent='false')]">
		<many-to-one>
			<xsl:choose>
				<xsl:when test="@plugin">
					<xsl:call-template name="entityName">
						<xsl:with-param name="modelName" select="@model" />
						<xsl:with-param name="pluginName" select="@plugin" />
					</xsl:call-template>
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="entityName">
						<xsl:with-param name="modelName" select="@model" />
						<xsl:with-param name="pluginName" select="/qcd:model/@plugin" />
					</xsl:call-template>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:attribute name="cascade">
			<xsl:choose>
				<xsl:when test="@cascade='delete'">delete</xsl:when>
				<xsl:otherwise>none</xsl:otherwise>
			</xsl:choose>
		</xsl:attribute>
			<xsl:attribute name="lazy">
			<xsl:choose>
				<xsl:when test="@lazy='false'">false</xsl:when>
				<xsl:otherwise>proxy</xsl:otherwise>
			</xsl:choose>
		</xsl:attribute>
			<xsl:call-template name="property" />
		</many-to-one>
	</xsl:template>

</xsl:stylesheet>