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

    private Configuration configuration;

    @Before
    public void setUp() {
        configuration = new Configuration();
        PackageStorageSetup packageStorageSetup = new PackageStorageSetup();
        packageStorageSetup.setEndpoint("endpoint");
        packageStorageSetup.setBucket("bucket");
        packageStorageSetup.setAccessKey("access_key");
        packageStorageSetup.setSecretAccessKey("secret_access_key");
        packageStorageSetup.setPackageDescriptor("runtime.yml");
        packageStorageSetup.setPackageFilename("runtime.zip");
        packageStorageSetup.setPrefix("runtimes/");

        configuration.setRuntimeStorage(packageStorageSetup);
        PackageStorageSetup configsStorageSetup = new PackageStorageSetup();
        configsStorageSetup.setEndpoint("endpoint");
        configsStorageSetup.setBucket("bucket");
        configsStorageSetup.setAccessKey("access_key");
        configsStorageSetup.setSecretAccessKey("secret_access_key");
        packageStorageSetup.setPackageDescriptor("config.yml");
        packageStorageSetup.setPackageFilename("config.zip");
        packageStorageSetup.setPrefix("configs/");
        configuration.setConfigStorage(configsStorageSetup);
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
    public void createStorages_withoutServiceProvider_usesDefaultImplementation() {
        MockedStatic<ServiceLoaderUtils> serviceLoaderMock = Mockito.mockStatic(ServiceLoaderUtils.class);
        serviceLoaderMock.when(() -> ServiceLoaderUtils.findFirst(StorageFactoryDescriptor.class))
                .thenReturn(Optional.empty());

        StorageFactory storageFactory = new StorageFactory();
        storageFactory.setConfiguration(configuration);
        Storage runtimeStorage = storageFactory.createRuntimeStorage();
        Storage configStorage = storageFactory.createConfigStorage();

        Assert.assertTrue(runtimeStorage instanceof S3PackageStorage);
        Assert.assertTrue(configStorage instanceof S3PackageStorage);

        serviceLoaderMock.close();
    }

}