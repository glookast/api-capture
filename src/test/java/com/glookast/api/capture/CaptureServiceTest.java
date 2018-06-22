package com.glookast.api.capture;

import com.glookast.commons.capture.PictureFormat;
import com.glookast.commons.templates.*;
import org.junit.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class CaptureServiceTest
{
    private static CaptureService captureService;

    @BeforeClass
    public static void setup()
    {
        captureService = new CaptureService("localhost", 4000);
    }

    @AfterClass
    public static void tearDown()
    {
    }

    @Before
    public void beforeMethod()
    {
//        captureService.getPictureFormats();
    }

    @After
    public void afterMethod()
    {
    }

    @Test
    public void getHostname()
    {
        Assert.assertEquals(captureService.getHostname(), "localhost");
    }

    @Test
    public void getPort()
    {
        Assert.assertEquals(captureService.getPort(), 4000);
    }

    @Test
    public void getObjectMapper()
    {
        Assert.assertNotNull(captureService.getObjectMapper());
    }


    @Test
    public void getPictureFormats()
    {
        List<PictureFormat> pictureFormats = null;

        try {
            pictureFormats = captureService.getPictureFormats();
        } catch (IOException | ApiException ignored) {
            Assert.fail();

        }

        Assert.assertNotNull(pictureFormats);
        Assert.assertFalse(pictureFormats.isEmpty());
    }

    @Test
    public void getContainerFormats()
    {
        List<ContainerFormat> containerFormats = null;

        try {
            containerFormats = captureService.getContainerFormats();
        } catch (IOException | ApiException ignored) {
            Assert.fail();
        }

        Assert.assertNotNull(containerFormats);
        Assert.assertFalse(containerFormats.isEmpty());
    }

    @Test
    public void getVideoFormats()
    {
        List<VideoFormat> videoFormats = null;

        try {
            videoFormats = captureService.getVideoFormats();
        } catch (IOException | ApiException ignored) {
            Assert.fail();
        }

        Assert.assertNotNull(videoFormats);
        Assert.assertFalse(videoFormats.isEmpty());
    }

    @Test
    public void getAudioFormats()
    {
        List<AudioFormat> audioFormats = null;

        try {
            audioFormats = captureService.getAudioFormats();
        } catch (IOException | ApiException ignored) {
            Assert.fail();
        }

        Assert.assertNotNull(audioFormats);
        Assert.assertFalse(audioFormats.isEmpty());
    }

    @Test
    public void getStorageSystems()
    {
        List<StorageSystem> storageSystems = null;

        try {
            storageSystems = captureService.getStorageSystems();
        } catch (IOException | ApiException ignored) {
            Assert.fail();
        }

        Assert.assertNotNull(storageSystems);
    }

    @Test
    public void getStorageSystem()
    {
        try {
            GenericStorageSystem genericStorageSystem = new GenericStorageSystem();
            genericStorageSystem.setName("Generic Storage");
            genericStorageSystem.setLocation("\\\\localhost\\C$\\temp");

            genericStorageSystem = (GenericStorageSystem) captureService.setStorageSystem(genericStorageSystem);

            StorageSystem storageSystem = captureService.getStorageSystem(genericStorageSystem.getId());

            Assert.assertEquals(storageSystem, genericStorageSystem);

            captureService.deleteStorageSystem(genericStorageSystem.getId());
        } catch (IOException | ApiException ignored) {
            Assert.fail();
        }
    }

    @Test
    public void setStorageSystem()
    {
        try {
            GenericStorageSystem genericStorageSystem = new GenericStorageSystem();
            genericStorageSystem.setName("Generic Storage");
            genericStorageSystem.setLocation("\\\\localhost\\C$\\temp");

            AvidNexisStorageSystem avidNexisStorageSystem = new AvidNexisStorageSystem();
            avidNexisStorageSystem.setName("Avid Nexis");
            avidNexisStorageSystem.setSystemDirector("VSD");
            avidNexisStorageSystem.setWorkspace("Media2");

            AvidMediaNetworkStorageSystem avidMediaNetworkStorageSystem = new AvidMediaNetworkStorageSystem();
            avidMediaNetworkStorageSystem.setId(UUID.randomUUID());
            avidMediaNetworkStorageSystem.setName("Media Network Storage System");
            avidMediaNetworkStorageSystem.setFileManager("Manager");
            avidMediaNetworkStorageSystem.setWorkspace("Workspace");

            AvidOtherStorageSystem avidOtherStorageSystem = new AvidOtherStorageSystem();
            avidOtherStorageSystem.setId(UUID.randomUUID());
            avidOtherStorageSystem.setName("Generic Avid Storage System");
            avidOtherStorageSystem.setLocation("\\\\localhost\\D$\\Avid MediaFiles\\MXF\\1");

            genericStorageSystem = (GenericStorageSystem) captureService.setStorageSystem(genericStorageSystem);
            avidNexisStorageSystem = (AvidNexisStorageSystem) captureService.setStorageSystem(avidNexisStorageSystem);
            avidMediaNetworkStorageSystem = (AvidMediaNetworkStorageSystem) captureService.setStorageSystem(avidMediaNetworkStorageSystem);
            avidOtherStorageSystem = (AvidOtherStorageSystem) captureService.setStorageSystem(avidOtherStorageSystem);

            StorageSystem storageSystem = captureService.getStorageSystem(genericStorageSystem.getId());
            Assert.assertEquals(storageSystem, genericStorageSystem);

            storageSystem = captureService.getStorageSystem(avidNexisStorageSystem.getId());
            Assert.assertEquals(storageSystem, avidNexisStorageSystem);

            storageSystem = captureService.getStorageSystem(avidMediaNetworkStorageSystem.getId());
            Assert.assertEquals(storageSystem, avidMediaNetworkStorageSystem);

            storageSystem = captureService.getStorageSystem(avidOtherStorageSystem.getId());
            Assert.assertEquals(storageSystem, avidOtherStorageSystem);

            captureService.deleteStorageSystem(genericStorageSystem.getId());
            captureService.deleteStorageSystem(avidNexisStorageSystem.getId());
            captureService.deleteStorageSystem(avidMediaNetworkStorageSystem.getId());
            captureService.deleteStorageSystem(avidOtherStorageSystem.getId());
        } catch (IOException | ApiException ignored) {
            Assert.fail();
        }
    }

    @Test
    public void deleteStorageSystem()
    {
        try {
            AvidNexisStorageSystem avidNexisStorageSystem = new AvidNexisStorageSystem();
            avidNexisStorageSystem.setName("Avid Nexis");
            avidNexisStorageSystem.setSystemDirector("VSD");
            avidNexisStorageSystem.setWorkspace("Media2");
            avidNexisStorageSystem.setWorkspace("\\\\localhost\\C$\\temp");

            avidNexisStorageSystem = (AvidNexisStorageSystem) captureService.setStorageSystem(avidNexisStorageSystem);

            captureService.deleteStorageSystem(avidNexisStorageSystem.getId());
        } catch (IOException | ApiException ignored) {
            Assert.fail();
        }
    }

    @Test
    public void testStorageSystem()
    {
        AvidNexisStorageSystem avidNexisStorageSystem = new AvidNexisStorageSystem();
        avidNexisStorageSystem.setName("Avid Nexis");
        avidNexisStorageSystem.setSystemDirector("VSD");
        avidNexisStorageSystem.setWorkspace("Media2");

        try {
            SystemTestResult systemTestResult = captureService.testStorageSystem(avidNexisStorageSystem);

            Assert.assertNotNull(systemTestResult);
        } catch (IOException | ApiException e) {
            Assert.fail();
        }
    }

    @Test
    public void testStorageSystem1()
    {
        AvidNexisStorageSystem avidNexisStorageSystem = new AvidNexisStorageSystem();
        avidNexisStorageSystem.setName("Avid Nexis");
        avidNexisStorageSystem.setSystemDirector("VSD");
        avidNexisStorageSystem.setWorkspace("Media2");

        try {
            avidNexisStorageSystem = (AvidNexisStorageSystem) captureService.setStorageSystem(avidNexisStorageSystem);

            SystemTestResult systemTestResult = captureService.testStorageSystem(avidNexisStorageSystem.getId());

            Assert.assertNotNull(systemTestResult);

            captureService.deleteStorageSystem(avidNexisStorageSystem.getId());
        } catch (IOException | ApiException e) {
            Assert.fail();
        }
    }

    @Test
    public void getMetadataSystems()
    {
        List<MetadataSystem> metadataSystems = null;

        try {
            metadataSystems = captureService.getMetadataSystems();
        } catch (IOException | ApiException ignored) {
            Assert.fail();
        }

        Assert.assertNotNull(metadataSystems);
    }

    @Test
    public void getMetadataSystem()
    {
    }

    @Test
    public void setMetadataSystem()
    {
    }

    @Test
    public void deleteMetadataSystem()
    {
    }

    @Test
    public void testMetadataSystem()
    {
    }

    @Test
    public void testMetadataSystem1()
    {
    }

    @Test
    public void getOutputSystems()
    {
    }

    @Test
    public void getOutputSystem()
    {
    }

    @Test
    public void setOutputSystem()
    {
    }

    @Test
    public void deleteOutputSystem()
    {
    }

    @Test
    public void getTransformProfiles()
    {
    }

    @Test
    public void getTransformProfile()
    {
    }

    @Test
    public void setTransformProfile()
    {
    }

    @Test
    public void deleteTransformProfile()
    {
    }

    @Test
    public void getTemplates()
    {
    }

    @Test
    public void getTemplate()
    {
    }

    @Test
    public void setTemplate()
    {
    }

    @Test
    public void deleteTemplate()
    {
    }

    @Test
    public void getBuffers()
    {
    }

    @Test
    public void getBuffers1()
    {
    }

    @Test
    public void getChannels()
    {
    }

    @Test
    public void getChannel()
    {
    }

    @Test
    public void getChannelConfiguration()
    {
    }

    @Test
    public void setChannelConfiguration()
    {
    }

    @Test
    public void restartChannel()
    {
    }

    @Test
    public void getTimecodes()
    {
    }

    @Test
    public void getPlayoutStatus()
    {
    }

    @Test
    public void getPlayoutConfiguration()
    {
    }

    @Test
    public void setPlayoutConfiguration()
    {
    }

    @Test
    public void playoutLoad()
    {
    }

    @Test
    public void playoutEject()
    {
    }

    @Test
    public void playoutPlayLive()
    {
    }

    @Test
    public void playoutPlay()
    {
    }

    @Test
    public void playoutPause()
    {
    }

    @Test
    public void playoutSeek()
    {
    }

    @Test
    public void playoutStep()
    {
    }

    @Test
    public void getCaptureJobs()
    {
    }

    @Test
    public void getCaptureJobs1()
    {
    }

    @Test
    public void getCaptureJobs2()
    {
    }

    @Test
    public void getCaptureJobs3()
    {
    }

    @Test
    public void getCaptureJobs4()
    {
    }

    @Test
    public void getCaptureJob()
    {
    }

    @Test
    public void createCaptureJob()
    {
    }

    @Test
    public void modifyCaptureJob()
    {
    }

    @Test
    public void modifyCaptureJob1()
    {
    }

    @Test
    public void modifyCaptureJob2()
    {
    }

    @Test
    public void modifyCaptureJob3()
    {
    }

    @Test
    public void modifyCaptureJob4()
    {
    }

    @Test
    public void deleteCaptureJob()
    {
    }

    @Test
    public void getCaptureJobFiles()
    {
    }

    @Test
    public void getCaptureJobLocators()
    {
    }

    @Test
    public void addCaptureJobLocator()
    {
    }

    @Test
    public void getCaptureJobMetadata()
    {
    }

    @Test
    public void setCaptureJobMetadata()
    {
    }

    @Test
    public void getCaptureJobThumbnail()
    {
    }

    @Test
    public void stopCaptureJob()
    {
    }

    @Test
    public void cancelCaptureJob()
    {
    }

    @Test
    public void restartCaptureJob()
    {
    }

    @Test
    public void getTransferJobs()
    {
    }

    @Test
    public void getTransferJobs1()
    {
    }

    @Test
    public void getTransferJobs2()
    {
    }

    @Test
    public void getTransferJobs3()
    {
    }

    @Test
    public void getTransferJob()
    {
    }

    @Test
    public void restartTransferJob()
    {
    }

    @Test
    public void getNotificationEndpoints()
    {
    }

    @Test
    public void getNotificationEndpoint()
    {
    }

    @Test
    public void setNotificationEndpoint()
    {
    }

    @Test
    public void deleteNotificationEndpoint()
    {
    }
}
