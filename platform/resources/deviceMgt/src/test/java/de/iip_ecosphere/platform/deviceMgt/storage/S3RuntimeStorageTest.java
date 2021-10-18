package de.iip_ecosphere.platform.deviceMgt.storage;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.http.Method;
import io.minio.messages.Contents;
import io.minio.messages.Item;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class S3RuntimeStorageTest {


    public static final String A_BUCKET = "abucket";
    public static final String PREFIX = "runtimes/";
    public static final String A_PATH = PREFIX + "A_PATH";
    public static final String RUNTIME_IMAGE_PATH = A_PATH + "/" + "runtime-image.zip";

    @Test
    public void getPrefix_shouldBeSetToRuntimes() {
        S3RuntimeStorage storage = new S3RuntimeStorage(null, null);
        Assert.assertEquals(PREFIX, storage.getPrefix());
    }

    @Test
    public void list_withMixedContent_onlyListsRuntimes() {
        Set<String> listing = validRuntimesListing();
        listing.add("runtimes/jkl/wrong-file.yml");
        MinioClient mc = mock(MinioClient.class);
        when(mc.listObjects(any())).thenReturn(setToResultIterable(listing));
        S3RuntimeStorage storage = new S3RuntimeStorage(mc, A_BUCKET);

        Assert.assertEquals(validRuntimesReducedListing(), storage.list());
    }

    @Test
    public void getDownloadUrl_withValidUrl_returnsUrl() throws Exception {
        MinioClient mc = mock(MinioClient.class);
        ArgumentCaptor<GetPresignedObjectUrlArgs> requestCaptor = ArgumentCaptor.forClass(GetPresignedObjectUrlArgs.class);
        when(mc.getPresignedObjectUrl(any())).thenReturn(RUNTIME_IMAGE_PATH);

        S3RuntimeStorage storage = new S3RuntimeStorage(mc, A_BUCKET);
        String downloadUrl = storage.generateDownloadUrl("A_PATH");

        verify(mc).getPresignedObjectUrl(requestCaptor.capture());
        GetPresignedObjectUrlArgs request = requestCaptor.getValue();

        Assert.assertEquals(RUNTIME_IMAGE_PATH, request.object());
        Assert.assertEquals(Method.GET, request.method());
        Assert.assertEquals(RUNTIME_IMAGE_PATH, downloadUrl);

    }

    private Iterable<Result<Item>> setToResultIterable(Set<String> objects) {
        return objects.stream().map(o -> new Result<Item>(new Contents(o))).collect(Collectors.toSet());
    }

    private Set<String> validRuntimesListing() {
        Set<String> listing = new HashSet<>();
        listing.add("runtimes/abc/runtime.yml");
        listing.add("runtimes/abc/runtime-image.zip");
        listing.add("runtimes/def/runtime.yml");
        listing.add("runtimes/def/runtime-image.zip");
        listing.add("runtimes/ghi/runtime.yml");
        listing.add("runtimes/ghi/runtime-image.zip");
        return listing;
    }

    private Set<String> validRuntimesReducedListing() {
        Set<String> listing = new HashSet<>();
        listing.add("runtimes/abc");
        listing.add("runtimes/def");
        listing.add("runtimes/ghi");
        return listing;
    }
}