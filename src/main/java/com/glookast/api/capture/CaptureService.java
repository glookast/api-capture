package com.glookast.api.capture;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.glookast.commons.capture.*;
import com.glookast.commons.templates.*;
import com.glookast.commons.timecode.Timecode;
import com.glookast.commons.timecode.TimecodeCollection;
import com.glookast.commons.timecode.TimecodeDuration;
import org.apache.commons.io.IOUtils;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.net.URLEncoder;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.net.HttpURLConnection.*;

@SuppressWarnings("WeakerAccess")
public class CaptureService
{
    private final String hostname;
    private final int port;

    private final String restEndpoint;
    private final ObjectMapper objectMapper;

    private final PoolingHttpClientConnectionManager connectionManager;
    private final IdleConnectionMonitorThread staleMonitor;
    private final CloseableHttpClient client;

    public CaptureService(String hostname, int port)
    {
        this(hostname, port, 8);
    }

    public CaptureService(String hostname, int port, int maxConnections)
    {
        this.hostname = hostname;
        this.port = port;

        this.restEndpoint = "http://" + hostname + ":" + port + "/api/v1/";

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new Jdk8Module());
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.setDateFormat(new StdDateFormat().withColonInTimeZone(true));
        this.objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setDefaultMaxPerRoute(maxConnections);
        connectionManager.setMaxTotal(maxConnections);
        connectionManager.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(5000).build());

        ConnectionKeepAliveStrategy keepAliveStrategy = (response, context) -> {
            HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
            while (it.hasNext()) {
                HeaderElement he = it.nextElement();
                String param = he.getName();
                String value = he.getValue();
                if (value != null && param.equalsIgnoreCase
                    ("timeout")) {
                    return Long.parseLong(value) * 1000;
                }
            }
            return 30 * 1000;
        };

        client = HttpClients.custom()
                            .setKeepAliveStrategy(keepAliveStrategy)
                            .setConnectionManager(connectionManager)
                            .build();

        staleMonitor = new IdleConnectionMonitorThread(connectionManager);
        staleMonitor.start();
    }

    public void close()
    {
        connectionManager.close();
        staleMonitor.shutdown();
    }

    public String getHostname()
    {
        return hostname;
    }

    public int getPort()
    {
        return port;
    }

    public ObjectMapper getObjectMapper()
    {
        return objectMapper;
    }

    public List<PictureFormat> getPictureFormats() throws IOException, ApiException
    {
        return getList("picture-formats", PictureFormat.class);
    }

    public List<ContainerFormat> getContainerFormats() throws IOException, ApiException
    {
        return getList("container-formats", ContainerFormat.class);
    }

    public List<VideoFormat> getVideoFormats() throws IOException, ApiException
    {
        return getList("video-formats", VideoFormat.class);
    }

    public List<AudioFormat> getAudioFormats() throws IOException, ApiException
    {
        return getList("audio-formats", AudioFormat.class);
    }

    public List<StorageSystem> getStorageSystems() throws IOException, ApiException
    {
        return getList("storage-systems", StorageSystem.class);
    }

    public StorageSystem getStorageSystem(UUID storageSystemId) throws IOException, ApiException
    {
        return get("storage-systems/" + storageSystemId, StorageSystem.class);
    }

    public StorageSystem setStorageSystem(StorageSystem storageSystem) throws IOException, ApiException
    {
        if (storageSystem.getId() == null) {
            return post("storage-systems", storageSystem, StorageSystem.class);
        } else {
            return put("storage-systems/" + storageSystem.getId(), storageSystem, StorageSystem.class);
        }
    }

    public void deleteStorageSystem(UUID storageSystemId) throws IOException, ApiException
    {
        delete("storage-systems/" + storageSystemId);
    }

    public SystemTestResult testStorageSystem(StorageSystem storageSystem) throws IOException, ApiException
    {
        return post("storage-systems/test", storageSystem, SystemTestResult.class);
    }

    public SystemTestResult testStorageSystem(UUID storageSystemId) throws IOException, ApiException
    {
        return post("storage-systems/" + storageSystemId + "/test", null, SystemTestResult.class);
    }

    public List<MetadataSystem> getMetadataSystems() throws IOException, ApiException
    {
        return getList("metadata-systems", MetadataSystem.class);
    }

    public MetadataSystem getMetadataSystem(UUID metadataSystemId) throws IOException, ApiException
    {
        return get("metadata-systems/" + metadataSystemId, MetadataSystem.class);
    }

    public MetadataSystem setMetadataSystem(MetadataSystem metadataSystem) throws IOException, ApiException
    {
        if (metadataSystem.getId() == null) {
            return post("metadata-systems", metadataSystem, MetadataSystem.class);
        } else {
            return put("metadata-systems/" + metadataSystem.getId(), metadataSystem, MetadataSystem.class);
        }
    }

    public void deleteMetadataSystem(UUID metadataSystemId) throws IOException, ApiException
    {
        delete("metadata-systems/" + metadataSystemId);
    }

    public SystemTestResult testMetadataSystem(MetadataSystem metadataSystem) throws IOException, ApiException
    {
        return post("metadata-systems/test", metadataSystem, SystemTestResult.class);
    }

    public SystemTestResult testMetadataSystem(UUID metadataSystemId) throws IOException, ApiException
    {
        return post("metadata-systems/" + metadataSystemId + "/test", null, SystemTestResult.class);
    }

    public List<OutputSystem> getOutputSystems() throws IOException, ApiException
    {
        return getList("output-systems", OutputSystem.class);
    }

    public OutputSystem getOutputSystem(UUID outputSystemId) throws IOException, ApiException
    {
        return get("output-systems/" + outputSystemId, OutputSystem.class);
    }

    public OutputSystem setOutputSystem(OutputSystem outputSystem) throws IOException, ApiException
    {
        if (outputSystem.getId() == null) {
            return post("output-systems", outputSystem, OutputSystem.class);
        } else {
            return put("output-systems/" + outputSystem.getId(), outputSystem, OutputSystem.class);
        }
    }

    public void deleteOutputSystem(UUID outputSystemId) throws IOException, ApiException
    {
        delete("output-systems/" + outputSystemId);
    }

    public List<TransformProfile> getTransformProfiles() throws IOException, ApiException
    {
        return getList("transform-profiles", TransformProfile.class);
    }

    public TransformProfile getTransformProfile(UUID transformProfileId) throws IOException, ApiException
    {
        return get("transform-profiles/" + transformProfileId, TransformProfile.class);
    }

    public TransformProfile setTransformProfile(TransformProfile transformProfile) throws IOException, ApiException
    {
        if (transformProfile.getId() == null) {
            return post("transform-profiles", transformProfile, TransformProfile.class);
        } else {
            return put("transform-profiles/" + transformProfile.getId(), transformProfile, TransformProfile.class);
        }
    }

    public void deleteTransformProfile(UUID transformProfileId) throws IOException, ApiException
    {
        delete("transform-profiles/" + transformProfileId);
    }

    public List<Template> getTemplates() throws IOException, ApiException
    {
        return getList("templates", Template.class);
    }

    public Template getTemplate(UUID templateId) throws IOException, ApiException
    {
        return get("templates/" + templateId, Template.class);
    }

    public Template setTemplate(Template template) throws IOException, ApiException
    {
        if (template.getId() == null) {
            return post("templates", template, Template.class);
        } else {
            return put("templates/" + template.getId(), template, Template.class);
        }
    }

    public void deleteTemplate(UUID templateId) throws IOException, ApiException
    {
        delete("templates/" + templateId);
    }

    public List<Buffer> getBuffers() throws IOException, ApiException
    {
        return getBuffers(null);
    }

    public List<Buffer> getBuffers(UUID templateId) throws IOException, ApiException
    {
        Map<String, Object> queryParams = new LinkedHashMap<>();

        if (templateId != null) {
            queryParams.put("templateId", templateId);
        }

        return getList("buffers", queryParams, Buffer.class);
    }

    public List<Channel> getChannels() throws IOException, ApiException
    {
        return getList("channels", Channel.class);
    }

    public Channel getChannel(int channelId) throws IOException, ApiException
    {
        return get("channels/" + channelId, Channel.class);
    }

    public ChannelConfiguration getChannelConfiguration(int channelId) throws IOException, ApiException
    {
        return get("channels/" + channelId + "/config", ChannelConfiguration.class);
    }

    public void setChannelConfiguration(int channelId, ChannelConfiguration channelConfiguration) throws IOException, ApiException
    {
        patch("channels/" + channelId + "/config", channelConfiguration);
    }

    public void restartChannel(int channelId) throws IOException, ApiException
    {
        post("channels/" + channelId + "/restart", null, null);
    }

    public TimecodeCollection getTimecodes(int channelId) throws IOException, ApiException
    {
        return get("channels/" + channelId + "/timecodes", TimecodeCollection.class);
    }

    public PlayoutStatus getPlayoutStatus(int channelId) throws IOException, ApiException
    {
        return get("channels/" + channelId + "/playout", PlayoutStatus.class);
    }

    public PlayoutConfiguration getPlayoutConfiguration(int channelId) throws IOException, ApiException
    {
        return get("channels/" + channelId + "/playout/config", PlayoutConfiguration.class);
    }

    public void setPlayoutConfiguration(int channelId, PlayoutConfiguration playoutConfiguration) throws IOException, ApiException
    {
        patch("channels/" + channelId + "/playout/config", playoutConfiguration);
    }

    public void playoutLoad(int channelId, UUID captureJobId) throws IOException, ApiException
    {
        post("channels/" + channelId + "/playout/load", captureJobId, null);
    }

    public void playoutEject(int channelId) throws IOException, ApiException
    {
        post("channels/" + channelId + "/playout/eject", null, null);
    }

    public void playoutPlayLive(int channelId) throws IOException, ApiException
    {
        playoutPlay(channelId, null);
    }

    public void playoutPlay(int channelId, Double playbackRate) throws IOException, ApiException
    {
        post("channels/" + channelId + "/playout/play", playbackRate, null);
    }

    public void playoutPause(int channelId) throws IOException, ApiException
    {
        post("channels/" + channelId + "/playout/pause", null, null);
    }

    public void playoutSeek(int channelId, long position) throws IOException, ApiException
    {
        post("channels/" + channelId + "/playout/seek", position, null);
    }

    public void playoutStep(int channelId, long distance) throws IOException, ApiException
    {
        post("channels/" + channelId + "/playout/step", distance, null);
    }

    public List<CaptureJob> getCaptureJobs() throws IOException, ApiException
    {
        return getCaptureJobs(null, null, null);
    }

    public List<CaptureJob> getCaptureJobs(int channelId) throws IOException, ApiException
    {
        return getCaptureJobs(channelId, null, null);
    }

    public List<CaptureJob> getCaptureJobs(String externalId) throws IOException, ApiException
    {
        return getCaptureJobs(null, externalId, null);
    }

    public List<CaptureJob> getCaptureJobs(CaptureJobStatus captureJobStatus) throws IOException, ApiException
    {
        return getCaptureJobs(null, null, captureJobStatus);
    }

    public List<CaptureJob> getCaptureJobs(Integer channelId, String externalId, CaptureJobStatus captureJobStatus) throws IOException, ApiException
    {
        Map<String, Object> queryParams = new LinkedHashMap<>();

        if (channelId != null) {
            queryParams.put("channelId", channelId);
        }
        if (externalId != null) {
            queryParams.put("externalId", externalId);
        }
        if (captureJobStatus != null) {
            queryParams.put("status", captureJobStatus);
        }

        return getList("capture-jobs", queryParams, CaptureJob.class);
    }

    public CaptureJob getCaptureJob(UUID captureJobId) throws IOException, ApiException
    {
        return get("capture-jobs/" + captureJobId, CaptureJob.class);
    }

    public CaptureJob createCaptureJob(CaptureJob captureJob) throws IOException, ApiException
    {
        return post("capture-jobs", captureJob, CaptureJob.class);
    }

    public void modifyCaptureJob(UUID captureJobId, CaptureJobPriority priority) throws IOException, ApiException
    {
        modifyCaptureJob(captureJobId, priority, null, null, null);
    }

    public void modifyCaptureJob(UUID captureJobId, String clipName) throws IOException, ApiException
    {
        modifyCaptureJob(captureJobId, null, clipName, null, null);
    }

    public void modifyCaptureJob(UUID captureJobId, Timecode endTimecode) throws IOException, ApiException
    {
        modifyCaptureJob(captureJobId, null, null, endTimecode, null);
    }

    public void modifyCaptureJob(UUID captureJobId, TimecodeDuration duration) throws IOException, ApiException
    {
        modifyCaptureJob(captureJobId, null, null, null, duration);
    }

    public void modifyCaptureJob(UUID captureJobId, CaptureJobPriority priority, String clipName, Timecode endTimecode, TimecodeDuration duration) throws IOException, ApiException
    {
        Map<String, Object> modifications = new LinkedHashMap<>();

        if (priority != null) {
            modifications.put("priority", priority);
        }
        if (clipName != null) {
            modifications.put("clipName", clipName);
        }
        if (endTimecode != null) {
            modifications.put("endTimecode", endTimecode);
        }
        if (duration != null) {
            modifications.put("duration", duration);
        }

        patch("capture-jobs/" + captureJobId, modifications);
    }

    public void deleteCaptureJob(UUID captureJobId) throws IOException, ApiException
    {
        delete("capture-jobs/" + captureJobId);
    }

    public List<FileCollection> getCaptureJobFiles(UUID captureJobId) throws IOException, ApiException
    {
        return getList("capture-jobs/" + captureJobId + "/files", FileCollection.class);
    }

    public List<Locator> getCaptureJobLocators(UUID captureJobId) throws IOException, ApiException
    {
        return getList("capture-jobs/" + captureJobId + "/locators", Locator.class);
    }

    public void addCaptureJobLocator(UUID captureJobId, Locator locator) throws IOException, ApiException
    {
        post("capture-jobs/" + captureJobId + "/locators", locator, null);
    }

    public DescriptiveMetadata getCaptureJobMetadata(UUID captureJobId) throws IOException, ApiException
    {
        return get("capture-jobs/" + captureJobId + "/metadata", DescriptiveMetadata.class);
    }

    public void setCaptureJobMetadata(UUID captureJobId, DescriptiveMetadata metadata) throws IOException, ApiException
    {
        patch("capture-jobs/" + captureJobId + "/metadata", metadata);
    }

    public byte[] getCaptureJobThumbnail(UUID captureJobId) throws IOException, ApiException
    {
        return get("capture-jobs/" + captureJobId + "/thumbnail", byte[].class);
    }

    public void stopCaptureJob(UUID captureJobId) throws IOException, ApiException
    {
        post("capture-jobs/" + captureJobId + "/stop", null, null);
    }

    public void cancelCaptureJob(UUID captureJobId) throws IOException, ApiException
    {
        post("capture-jobs/" + captureJobId + "/cancel", null, null);
    }

    public void restartCaptureJob(UUID captureJobId) throws IOException, ApiException
    {
        post("capture-jobs/" + captureJobId + "/restart", null, null);
    }

    public List<TransferJob> getTransferJobs() throws IOException, ApiException
    {
        return getTransferJobs(null, null);
    }

    public List<TransferJob> getTransferJobs(Integer channelId) throws IOException, ApiException
    {
        return getTransferJobs(channelId, null);
    }

    public List<TransferJob> getTransferJobs(UUID captureJobId) throws IOException, ApiException
    {
        return getTransferJobs(null, captureJobId);
    }

    public List<TransferJob> getTransferJobs(Integer channelId, UUID captureJobId) throws IOException, ApiException
    {
        Map<String, Object> queryParams = new LinkedHashMap<>();

        if (channelId != null) {
            queryParams.put("channelId", channelId);
        }
        if (captureJobId != null) {
            queryParams.put("captureJobId", captureJobId);
        }

        return getList("transfer-jobs", queryParams, TransferJob.class);
    }

    public TransferJob getTransferJob(String transferJobId) throws IOException, ApiException
    {
        return get("transfer-jobs/" + transferJobId, TransferJob.class);
    }

    public void restartTransferJob(String transferJobId) throws IOException, ApiException
    {
        post("transfer-jobs/" + transferJobId + "/restart", null, null);
    }

    public List<NotificationEndpoint> getNotificationEndpoints() throws IOException, ApiException
    {
        return getList("notification-endpoints", NotificationEndpoint.class);
    }

    public NotificationEndpoint getNotificationEndpoint(UUID notificationEndpointId) throws IOException, ApiException
    {
        return get("notification-endpoints/" + notificationEndpointId, NotificationEndpoint.class);
    }

    public NotificationEndpoint setNotificationEndpoint(NotificationEndpoint notificationEndpoint) throws IOException, ApiException
    {
        if (notificationEndpoint.getId() == null) {
            return post("notification-endpoints", notificationEndpoint, NotificationEndpoint.class);
        } else {
            return put("notification-endpoints/" + notificationEndpoint.getId(), notificationEndpoint, NotificationEndpoint.class);
        }
    }

    public void deleteNotificationEndpoint(UUID notificationEndpointId) throws IOException, ApiException
    {
        delete("notification-endpoints/" + notificationEndpointId);
    }

    private <R> List<R> getList(String path, Class<R> responseType) throws IOException, ApiException
    {
        return method(HttpMethod.GET, path, null, null, responseType);
    }

    private <R> List<R> getList(String path, Map<String, Object> queryParams, Class<R> responseType) throws IOException, ApiException
    {
        return method(HttpMethod.GET, path, queryParams, null, responseType);
    }

    private <R> R get(String path, Class<R> responseType) throws IOException, ApiException
    {
        List<R> list = method(HttpMethod.GET, path, null, null, responseType);

        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    private <T, R> R post(String path, T requestBody, Class<R> responseType) throws IOException, ApiException
    {
        List<R> list = method(HttpMethod.POST, path, null, requestBody, responseType);

        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    private <T, R> R put(String path, T requestBody, Class<R> responseType) throws IOException, ApiException
    {
        List<R> list = method(HttpMethod.PUT, path, null, requestBody, responseType);

        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    private <T> void patch(String path, T requestBody) throws IOException, ApiException
    {
        method(HttpMethod.PATCH, path, null, requestBody, null);
    }

    private void delete(String path) throws IOException, ApiException
    {
        method(HttpMethod.DELETE, path, null, null, null);
    }

    @SuppressWarnings("unchecked")
    private <T, R> List<R> method(HttpMethod method, String path, Map<String, Object> queryParams, T requestBody, Class<R> responseType) throws IOException, ApiException
    {
        StringBuilder url = new StringBuilder(restEndpoint + path);

        if (queryParams != null) {
            boolean isFirstIteration = true;

            for (Map.Entry<String, Object> e : queryParams.entrySet()) {
                url.append(isFirstIteration ? "?" : "&");
                url.append(URLEncoder.encode(e.getKey(), "UTF-8"));
                url.append("=");
                url.append(URLEncoder.encode(String.valueOf(e.getValue()), "UTF-8"));
                isFirstIteration = false;
            }
        }

        StringEntity requestEntity = null;

        if (requestBody != null) {
            String requestBodyJson = this.objectMapper.writerWithDefaultPrettyPrinter()
                                                      .writeValueAsString(requestBody);
            requestEntity = new StringEntity(requestBodyJson);
        }

        CloseableHttpResponse response;

        switch (method) {
            case GET:
                HttpGet httpGet = new HttpGet(url.toString());
                httpGet.setHeader("Accept", "application/json");
                response = client.execute(httpGet);
                break;
            case POST:
                HttpPost httpPost = new HttpPost(url.toString());
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");
                httpPost.setEntity(requestEntity);
                response = client.execute(httpPost);
                break;
            case PUT:
                HttpPut httpPut = new HttpPut(url.toString());
                httpPut.setHeader("Accept", "application/json");
                httpPut.setHeader("Content-type", "application/json");
                httpPut.setEntity(requestEntity);
                response = client.execute(httpPut);
                break;
            case PATCH:
                HttpPatch httpPatch = new HttpPatch(url.toString());
                httpPatch.setHeader("Accept", "application/json");
                httpPatch.setHeader("Content-type", "application/merge-patch+json");
                httpPatch.setEntity(requestEntity);
                response = client.execute(httpPatch);
                break;
            case DELETE:
                HttpDelete httpDelete = new HttpDelete(url.toString());
                httpDelete.setHeader("Accept", "application/json");
                response = client.execute(httpDelete);
                break;
            default:
                throw new IllegalArgumentException("Method '" + method + "' not implemented");
        }

        HttpEntity responseEntity = response.getEntity();

        JsonNode jsonNode = null;
        byte[] rawData = null;

        if (responseEntity.getContent() != null) {
            boolean isJson = false;

            if (responseEntity.getContentType() != null) {
                for (HeaderElement headerElement : responseEntity.getContentType().getElements()) {
                    if (Objects.equals(headerElement.getName(), "application/json")) {
                        isJson = true;
                    }
                }

                if (isJson) {
                    jsonNode = objectMapper.readTree(responseEntity.getContent());
                } else {
                    rawData = IOUtils.toByteArray(responseEntity.getContent());
                }
            }
        }

        int status = response.getStatusLine().getStatusCode();
        List<R> result = responseType != null ? new ArrayList<>() : null;

        if (status / 100 * 100 == 200) {
            switch (status) {
                case HTTP_OK:
                case HTTP_CREATED:
                    if (result != null) {
                        if (jsonNode != null) {
                            if (jsonNode.isArray()) {
                                for (JsonNode node : jsonNode) {
                                    result.add(objectMapper.treeToValue(node, responseType));
                                }
                            } else {
                                result.add(objectMapper.treeToValue(jsonNode, responseType));
                            }
                        }
                        if (rawData != null && Objects.equals(responseType, byte[].class)) {
                            result.add((R) rawData);
                        }
                    }
                    break;
                case HTTP_ACCEPTED:
                    break;
            }
        } else {
            ApiError apiError = null;
            if (jsonNode != null) {
                try {
                    apiError = objectMapper.treeToValue(jsonNode, ApiError.class);
                } catch (Exception ignored) {
                }
            }

            if (apiError == null) {
                apiError = new ApiError();
                apiError.setTimestamp(OffsetDateTime.now());
                apiError.setStatus(status);
                apiError.setError(response.getStatusLine().getReasonPhrase());
                apiError.setPath(restEndpoint + path);
            }

            throw new ApiException(apiError);
        }

        return result;
    }

    private enum HttpMethod
    {
        OPTIONS,
        GET,
        HEAD,
        POST,
        PUT,
        PATCH,
        DELETE
    }

    private class IdleConnectionMonitorThread extends Thread
    {
        private final HttpClientConnectionManager connectionManager;
        private volatile boolean shutdown;

        public IdleConnectionMonitorThread(PoolingHttpClientConnectionManager connectionManager)
        {
            this.connectionManager = connectionManager;
        }

        @Override
        public void run()
        {
            try {
                while (!shutdown) {
                    synchronized (this) {
                        wait(1000);
                        connectionManager.closeExpiredConnections();
                        connectionManager.closeIdleConnections(30, TimeUnit.SECONDS);
                    }
                }
            } catch (InterruptedException ex) {
                shutdown();
            }
        }

        public void shutdown()
        {
            shutdown = true;
            synchronized (this) {
                notifyAll();
            }
        }
    }
}
