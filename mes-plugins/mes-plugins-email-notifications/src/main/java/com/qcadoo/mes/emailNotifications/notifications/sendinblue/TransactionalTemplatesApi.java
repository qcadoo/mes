package com.qcadoo.mes.emailNotifications.notifications.sendinblue;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Response;
import sendinblue.*;
import sibModel.SendEmail;
import sibModel.SendTemplateEmail;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TransactionalTemplatesApi {

    private ApiClient apiClient;

    public TransactionalTemplatesApi() {
        this(Configuration.getDefaultApiClient());
    }

    public TransactionalTemplatesApi(final ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return this.apiClient;
    }

    public void setApiClient(final ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Build call for sendTemplate
     *
     * @param templateId              Id of the template (required)
     * @param sendEmail               (required)
     * @param progressListener        Progress listener
     * @param progressRequestListener Progress request listener
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     */
    public Call sendTemplateCall(final Long templateId, final SendEmail sendEmail, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {
        Object localVarPostBody = sendEmail;

        // create path and map variables
        String localVarPath = "/smtp/templates/{templateId}/send".replaceAll("\\{" + "templateId" + "\\}", apiClient.escapeString(templateId.toString()));

        List<Pair> localVarQueryParams = Lists.newArrayList();
        List<Pair> localVarCollectionQueryParams = Lists.newArrayList();
        Map<String, String> localVarHeaderParams = Maps.newHashMap();
        Map<String, Object> localVarFormParams = Maps.newHashMap();

        final String[] localVarAccepts = {"application/json"};
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

        if (Objects.nonNull(localVarAccept)) {
            localVarHeaderParams.put("Accept", localVarAccept);
        }

        final String[] localVarContentTypes = {"application/json"};

        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        localVarHeaderParams.put("Content-Type", localVarContentType);

        if (Objects.nonNull(progressListener)) {
            apiClient.getHttpClient().networkInterceptors().add(chain -> {
                Response originalResponse = chain.proceed(chain.request());

                return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                        .build();
            });
        }

        String[] localVarAuthNames = new String[]{"api-key", "partner-key"};

        return apiClient.buildCall(localVarPath, "POST", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAuthNames, progressRequestListener);
    }

    @SuppressWarnings("rawtypes")
    private Call sendTemplateValidateBeforeCall(final Long templateId, final SendEmail sendEmail, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {
        // verify the required parameter 'templateId' is set
        if (Objects.isNull(templateId)) {
            throw new ApiException("Missing the required parameter 'templateId' when calling sendTemplate(Async)");
        }

        // verify the required parameter 'sendEmail' is set
        if (Objects.isNull(sendEmail)) {
            throw new ApiException("Missing the required parameter 'sendEmail' when calling sendTemplate(Async)");
        }

        Call call = sendTemplateCall(templateId, sendEmail, progressListener, progressRequestListener);

        return call;
    }

    /**
     * Send a template
     *
     * @param templateId Id of the template (required)
     * @param sendEmail  (required)
     * @return SendTemplateEmail
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public SendTemplateEmail sendTemplate(final Long templateId, final SendEmail sendEmail) throws ApiException {
        ApiResponse<SendTemplateEmail> resp = sendTemplateWithHttpInfo(templateId, sendEmail);

        return resp.getData();
    }

    /**
     * Send a template
     *
     * @param templateId Id of the template (required)
     * @param sendEmail  (required)
     * @return ApiResponse&lt;SendTemplateEmail&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public ApiResponse<SendTemplateEmail> sendTemplateWithHttpInfo(final Long templateId, final SendEmail sendEmail) throws ApiException {
        Call call = sendTemplateValidateBeforeCall(templateId, sendEmail, null, null);

        Type localVarReturnType = new TypeToken<SendTemplateEmail>() {}.getType();

        return apiClient.execute(call, localVarReturnType);
    }

    /**
     * Send a template (asynchronously)
     *
     * @param templateId Id of the template (required)
     * @param sendEmail  (required)
     * @param callback   The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     */
    public Call sendTemplateAsync(final Long templateId, final SendEmail sendEmail, final ApiCallback<Void> callback) throws ApiException {
        ProgressResponseBody.ProgressListener progressListener = null;
        ProgressRequestBody.ProgressRequestListener progressRequestListener = null;

        if (Objects.nonNull(callback)) {
            progressListener = callback::onDownloadProgress;

            progressRequestListener = callback::onUploadProgress;
        }

        Call call = sendTemplateValidateBeforeCall(templateId, sendEmail, progressListener, progressRequestListener);

        apiClient.executeAsync(call, callback);

        return call;
    }

}