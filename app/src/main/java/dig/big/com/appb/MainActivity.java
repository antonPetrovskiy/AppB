package dig.big.com.appb;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    int i = 10;
    Handler toastHandlerExit;
    Handler toastHandlerRemove;
    Timer timer;
    Toast toast;
    ImageView image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        if(getIntent().getStringExtra("fromA")!=null){
            checkUserPermission();
            if(getIntent().getStringExtra("fromA").equals("test"))
                addLink();
            if(getIntent().getStringExtra("fromA").equals("history"))
                updateLink();
        }else{
            toast = Toast.makeText(getApplicationContext(),
                    "приложение В не является самостоятельным приложением и будет закрыто через " +i+ " секунд", Toast.LENGTH_SHORT);
            counterExit();
            timer = new Timer();
            timer.schedule(new UpdateTimeTask(), 0, 1000);
        }
    }

    public void init(){
        image = findViewById(R.id.imageView);
    }

    public void addLink(){
        Picasso.with(getApplicationContext()).load(getIntent().getStringExtra("link")).error(R.drawable.photo1).into(image, new Callback() {
            @Override
            public void onSuccess() {
                Intent it = new Intent("dig.big.com.appa.add");
                it.putExtra("link",getIntent().getStringExtra("link"));
                it.putExtra("status","1");
                it.putExtra("time",Calendar.getInstance().getTimeInMillis()+"");
                getApplicationContext().sendBroadcast(it);
            }

            @Override
            public void onError() {
                Intent it = new Intent("dig.big.com.appa.add");
                it.putExtra("link",getIntent().getStringExtra("link"));
                it.putExtra("status","2");
                it.putExtra("time",Calendar.getInstance().getTimeInMillis()+"");
                getApplicationContext().sendBroadcast(it);
            }

        });

    }

    public void updateLink(){
        if(getIntent().getStringExtra("status").equals("1")){
            Picasso.with(getApplicationContext()).load(getIntent().getStringExtra("link")).error(R.drawable.photo1).into(image);
            counterRemove();
            timer = new Timer();
            timer.schedule(new UpdateTimeTask(), 0, 1000);
        }else{
            Picasso.with(getApplicationContext()).load(getIntent().getStringExtra("link")).error(R.drawable.photo1).into(image, new Callback() {
                @Override
                public void onSuccess() {
                    Intent it = new Intent("dig.big.com.appa.update");
                    it.putExtra("link",getIntent().getStringExtra("link"));
                    it.putExtra("status","1");
                    it.putExtra("time",Calendar.getInstance().getTimeInMillis()+"");
                    getApplicationContext().sendBroadcast(it);
                }

                @Override
                public void onError() {

                }

            });
        }
    }

    public void counterRemove(){
        toastHandlerRemove = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                if(i==-5){
                    Intent it = new Intent("dig.big.com.appa.remove");
                    it.putExtra("link",getIntent().getStringExtra("link"));
                    it.putExtra("status",getIntent().getStringExtra("status"));
                    it.putExtra("time",getIntent().getStringExtra("time"));
                    getApplicationContext().sendBroadcast(it);
                    toast = Toast.makeText(getApplicationContext(),
                            "ссылка была удалена", Toast.LENGTH_SHORT);
                    toast.show();
                    timer.cancel();

                    Picasso.with(getApplicationContext())
                            .load(getIntent().getStringExtra("link"))
                            .into(getTarget(getIntent().getStringExtra("time")));
                }
            }
        };
    }

    public void counterExit(){
        toastHandlerExit = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                if(i<5 && i>1){
                    toast.setText("приложение В не является самостоятельным приложением и будет закрыто через " +i+ " секунды");
                    toast.show();
                }else if(i == 1){
                    toast.setText("приложение В не является самостоятельным приложением и будет закрыто через " +i+ " секунду");
                    toast.show();
                }else if(i==0) {
                    timer.cancel();
                    finishAffinity();
                }else{
                    toast.setText("приложение В не является самостоятельным приложением и будет закрыто через " +i+ " секунд");
                    toast.show();
                }
            }
        };
    }

    class UpdateTimeTask extends TimerTask {
        public void run() {
            if(toastHandlerExit!=null)
                toastHandlerExit.sendEmptyMessage(0);
            if(toastHandlerRemove!=null)
                toastHandlerRemove.sendEmptyMessage(0);
            i--;
        }
    }

    //target to save image
    private static Target getTarget(final String url){
        Target target = new Target(){
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        File sdPath = Environment.getExternalStorageDirectory();
                        // добавляем свой каталог к пути
                        sdPath = new File(sdPath.getAbsolutePath() + "/" + "sdcard/BIGDIG/test/B");
                        // создаем каталог
                        sdPath.mkdirs();

                        File file = new File(sdPath, url+".png");
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                fos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();

            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
        return target;
    }

    private void checkUserPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);
            }



        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 123:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    checkUserPermission();
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }

    }
}
