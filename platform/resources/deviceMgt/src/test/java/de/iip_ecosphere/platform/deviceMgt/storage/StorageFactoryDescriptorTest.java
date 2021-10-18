package de.iip_ecosphere.platform.deviceMgt.storage;

import de.iip_ecosphere.platform.deviceMgt.Configuration;
import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StorageFactoryDescriptorTest {

    public static final String A_PATH = "A_PATH";
    public static final String AN_ENDPOINT = "endpoint";
    public static final String AN_ACCESS_KEY = "AN_ACCESS_KEY";
    public static final String AN_SECRET_ACCESS_KEY = "AN_SECRET_ACCESS_KEY";

    private Configuration configuration;

    @Before
    public void setUp() {
        configuration = new Configuration();
        StorageSetup storageSetup = new StorageSetup();
        storageSetup.setEndpoint("endpoint");
        storageSetup.setBucket("bucket");
        storageSetup.setAccessKey("access_key");
        storageSetup.setSecretAccessKey("secret_access_key");
        configuration.setStorage(storageSetup);
    }

    @Test
    public void createRuntimeStorage_withConfiguration_createsStorage() {
        S3StorageFactoryDescriptor s3StorageFactoryDescriptor = new S3StorageFactoryDescriptor();
        Storage storage = s3StorageFactoryDescriptor.createRuntimeStorage(configuration);
        Assert.assertNotNull(storage);
    }

    @Test
    public void createRuntimeStorage_withInvalidConfiguration_returnsNull() {
        S3StorageFactoryDescriptor s3StorageFactoryDescriptor = new S3StorageFactoryDescriptor();
        Storage storage = s3StorageFactoryDescriptor.createRuntimeStorage(null);
        Assert.assertNull(storage);
    }

    @Test
    public void createRuntimeStorage_withServiceProvider_usesServiceProvider() {
        MockedStatic<ServiceLoaderUtils> serviceLoaderMock = Mockito.mockStatic(ServiceLoaderUtils.class);
        StorageFactoryDescriptor storageFactoryDescriptor = mock(StorageFactoryDescriptor.class);
        S3Storage storage = new S3Storage(null, null, null);
        when(storageFactoryDescriptor.createRuntimeStorage(any())).thenReturn(storage);

        serviceLoaderMock.when(() -> ServiceLoaderUtils.findFirst(StorageFactoryDescriptor.class))
                .thenReturn(Optional.of(storageFactoryDescriptor));

        StorageFactory storageFactory = new StorageFactory();
        Storage runtimeStorage = storageFactory.createRuntimeStorage();
        Assert.assertEquals(storage, runtimeStorage);

        serviceLoaderMock.close();
    }

    @Test
    public void createRuntimeStorage_withoutServiceProvider_usesDefaultImplementation() {
        MockedStatic<ServiceLoaderUtils> serviceLoaderMock = Mockito.mockStatic(ServiceLoaderUtils.class);
        serviceLoaderMock.when(() -> ServiceLoaderUtils.findFirst(StorageFactoryDescriptor.class))
                .thenReturn(Optional.empty());

        StorageFactory storageFactory = new StorageFactory();
        storageFactory.setConfiguration(configuration);
        Storage runtimeStorage = storageFactory.createRuntimeStorage();

        Assert.assertTrue(runtimeStorage instanceof S3PackageStorage);

        serviceLoaderMock.close();
    }

}