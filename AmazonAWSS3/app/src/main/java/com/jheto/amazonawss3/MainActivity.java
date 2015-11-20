package com.jheto.amazonawss3;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.FileOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //**********************************************************************************************
    //Test all this in debug mode, and see the problem ;)

    private final static String identityPoolId = "CHANGE FOR YOU POOL ID";
    private final static String bucketName = "CHANGE FOR YOU BUCKET NAME";
    private static CognitoCachingCredentialsProvider sCredProvider = null;

    private static CognitoCachingCredentialsProvider getCredProvider(Context context) {
        if (sCredProvider == null) sCredProvider = new CognitoCachingCredentialsProvider(context.getApplicationContext(), identityPoolId, Regions.US_EAST_1);
        return sCredProvider;
    }

    interface S3BitmapDownload {
        void download(Bitmap asset, Exception e);
    }

    interface S3BitmapUpload {
        void upload(String assetID, Exception e);
    }

    interface S3BitmapRemove {
        void remove(String assetID, Exception e);
    }

    interface S3BitmapList {
        void list(List<String> assetID, Exception e);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button buttonGetFile = (Button)findViewById(R.id.buttonGetFile);
        Button buttonPutFile = (Button)findViewById(R.id.buttonPutFile);
        Button buttonRemoveFile = (Button)findViewById(R.id.buttonRemoveFile);
        Button buttonListFiles = (Button)findViewById(R.id.buttonListFiles);

        buttonGetFile.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                downloadBitmapS3();
            }
        });
        buttonPutFile.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                uploadBitmapS3();
            }
        });
        buttonRemoveFile.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                removeBitmapS3();
            }
        });
        buttonListFiles.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                listBitmapS3();
            }
        });
    }

    //**********************************************************************************************

    private void getBitmap(String bucketName, String key, final S3BitmapDownload callback){
        final String bucketNameFinal = (bucketName != null && bucketName.length()>0)? bucketName:null;
        final String keyFinal = (key != null && key.length()>0)? key:null;
        try{
            final AmazonS3 s3Client = new AmazonS3Client(getCredProvider(getApplicationContext()));
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Bitmap bmp = null;
                    try{
                        GetObjectRequest gor = new GetObjectRequest(bucketNameFinal, keyFinal);
                        S3Object s3o = s3Client.getObject(gor);
                        S3ObjectInputStream s3is = s3o.getObjectContent();
                        bmp = BitmapFactory.decodeStream(s3is);
                        s3is.close();
                    }catch(Exception e){
                        bmp = null;
                        if(callback != null) callback.download(null, e);
                    }
                    if(callback != null && bmp != null) callback.download(bmp, null);
                }
            }).start();
        }catch(Exception e){
            if(callback != null) callback.download(null, e);
        }
    }

    private void putBitmap(Bitmap asset, String bucketName, String key, final S3BitmapUpload callback){
        final String bucketNameFinal = (bucketName != null && bucketName.length()>0)? bucketName:null;
        final String keyFinal = (key != null && key.length()>0)? key:null;
        try{
            if(asset.getWidth()>0 && asset.getHeight()>0){
                final AmazonS3 s3Client = new AmazonS3Client(getCredProvider(getApplicationContext()));
                final File temp = File.createTempFile("upload", ".tmp");
                FileOutputStream fos = new FileOutputStream(temp);
                asset.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            PutObjectRequest por = new PutObjectRequest(bucketNameFinal, keyFinal, temp);
                            PutObjectResult result = s3Client.putObject(por);
                            String etag = result.getETag();
                            if(etag != null && etag.length()>0){
                                if(temp.exists()) temp.delete();
                                if(callback != null) callback.upload(etag, null);
                            }
                            else throw new NullPointerException("eTag is empty");
                        }catch(Exception e){
                            if(temp.exists()) temp.delete();
                            if(callback != null) callback.upload(null, e);
                        }
                    }
                }).start();
            }
        }catch(Exception e){
            if(callback != null) callback.upload(null, e);
        }

    }

    private void removeBitmap(String bucketName, String key, final S3BitmapRemove callback){
        final String bucketNameFinal = (bucketName != null && bucketName.length()>0)? bucketName:null;
        final String keyFinal = (key != null && key.length()>0)? key:null;
        try{
            final AmazonS3 s3Client = new AmazonS3Client(getCredProvider(getApplicationContext()));
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        DeleteObjectRequest dor = new DeleteObjectRequest(bucketNameFinal, keyFinal);
                        s3Client.deleteObject(dor);
                        if(callback != null) callback.remove(keyFinal, null);
                    }catch(Exception e){
                        if(callback != null) callback.remove(null, e);
                    }
                }
            }).start();
        }catch(Exception e){
            if(callback != null) callback.remove(null, e);
        }
    }

    private void listBitmap(String bucketName, final S3BitmapList callback){
        final String bucketNameFinal = (bucketName != null && bucketName.length()>0)? bucketName:null;
        try{
            final AmazonS3 s3Client = new AmazonS3Client(getCredProvider(getApplicationContext()));
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketNameFinal).withPrefix("t");
                        ObjectListing objectListing = s3Client.listObjects(listObjectsRequest);
                        List<S3ObjectSummary> list =  objectListing.getObjectSummaries();
                        if(list.size()>0){
                            List<String> items = new ArrayList<String>();
                            for(S3ObjectSummary element:list){
                                String key = element.getKey();
                                if(!items.contains(key)) items.add(key);
                            }
                            if(callback != null) callback.list(items, null);
                        }
                        else throw new NullPointerException("list is empty");
                    }catch(Exception e){
                        if(callback != null) callback.list(null, e);
                    }
                }
            }).start();
        }catch(Exception e){
            if(callback != null) callback.list(null, e);
        }
    }

    private void downloadBitmapS3(){
        getBitmap(MainActivity.bucketName, "test", new S3BitmapDownload() {
            @Override
            public void download(Bitmap asset, Exception e) {
                if (e == null && asset != null) {
                    String dim = asset.getWidth() + "x" + asset.getHeight();
                    Log.e("DIM", dim);
                } else if (e != null) {
                    Log.e("Exception", e.toString());
                }
            }
        });
    }

    private void uploadBitmapS3(){
        Bitmap asset = null;
        try{
            asset = ((BitmapDrawable)getPackageManager().getApplicationIcon(getPackageName())).getBitmap();
        }catch(Exception e){
            Log.e("Exception", e.toString());
        }
        putBitmap(asset, MainActivity.bucketName, "test", new S3BitmapUpload() {
            @Override
            public void upload(String assetID, Exception e) {
                if (e == null && assetID != null && assetID.length() > 0) {
                    Log.e("assetID", assetID);
                } else if (e != null) {
                    Log.e("Exception", e.toString());
                }
            }
        });
    }

    private void removeBitmapS3(){
        removeBitmap(MainActivity.bucketName, "test", new S3BitmapRemove() {
            @Override
            public void remove(String assetID, Exception e) {
                if (e == null && assetID != null && assetID.length() > 0) {
                    Log.e("assetID", assetID);
                } else if (e != null) {
                    Log.e("Exception", e.toString());
                }
            }
        });
    }

    private void listBitmapS3(){
        listBitmap(MainActivity.bucketName, new S3BitmapList() {
            @Override
            public void list(List<String> assetID, Exception e) {
                if (e == null && assetID != null && assetID.size() > 0) {
                    Log.e("assetsID", assetID.size()+"");
                } else if (e != null) {
                    Log.e("Exception", e.toString());
                }
            }
        });
    }

    //**********************************************************************************************

}
