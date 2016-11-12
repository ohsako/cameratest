package com.example.sa.cameratest;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.testcameraintent.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

// 位置情報用imprort宣言(未使用)

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

public class MainActivity extends AppCompatActivity {

    private final static int RESULT_CAMERA = 1001;
    private final static int REQUEST_PERMISSION = 1002;

    private ImageView imageView;
    private Uri cameraUri;
    private File  cameraFile;
    private String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null){
            cameraUri = savedInstanceState.getParcelable("CaptureUri");
        }

        imageView = (ImageView)findViewById(R.id.image_view);

        Button cameraButton = (Button)findViewById(R.id.camera_button);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Android 6, API 23以上でパーミッシンの確認
                if (Build.VERSION.SDK_INT >= 23) {
                    checkPermission();
                }
                else {
                    cameraIntent();
                }
            }
        });
    }

    protected void onSaveInstanceState(Bundle outState){
        outState.putParcelable("CaptureUri", cameraUri);
    }

    private void cameraIntent(){
        // 保存先のフォルダーを作成
        File cameraFolder = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "IMG"
        );
        cameraFolder.mkdirs();

        // 保存ファイル名
        String fileName = new SimpleDateFormat("ddHHmmss").format(new Date());
        filePath = cameraFolder.getPath() +"/" + fileName + ".jpg";
        Log.d("debug","filePath:"+filePath);

        // 画像のファイルパス
        cameraFile = new File(filePath);
        cameraUri = Uri.fromFile(cameraFile);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
        startActivityForResult(intent, RESULT_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_CAMERA) {
            if(cameraUri != null){
                imageView.setImageURI(cameraUri);
            }
            else{
                Log.d("debug","cameraUri == null");
            }
        }

    }

    /**
     * checkPermission()というメソッド名の場合、多くは、check結果を戻り値として返すことが多いです。
     * 理由は様々あると思っていますが、一番は、メソッドとしての役割の明確化だと考えています。
     * 例えば、チェックする場合、結果として何かほしい場合が多いと思っています（プログラムにかぎらず）
     * Javaはオブジェクト指向言語ですので、そのように考えることが望まれていると理解しています（伊藤は）。
     */
    // Runtime Permission check
    private void checkPermission(){
        // 既に許可している
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
            cameraIntent();
        }
        // 拒否していた場合
        else{
            requestLocationPermission();
        }
    }

    // 許可を求める
    private void requestLocationPermission() {
        /**
         * 条件がfalseになる場合はどんな状況の場合でしょうか？
         */
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);

        } else {
            Toast toast = Toast.makeText(this, "実行には許可が必須です", Toast.LENGTH_SHORT);
            toast.show();

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,}, REQUEST_PERMISSION);

        }
    }

    // 結果の受け取り
    /* 下記の様な JavaDoc 用のコメントを入力を心がけると業務では良いです */
    /**
     * 権限要求からのコールバックを継承
     * @param requestCode   リクエストコード
     * @param permissions   付与されるパーミッション
     * @param grantResults  付与された結果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        /**
         * else文を書かなくていいのか、書いたほうがいいのかを常に意識しておくといいかもです。
         * 今回の場合は、権限要求が一つなら書く必要がないですね。
         *
         * ただし、int型の requestCode が返却されるので switch文も考えてもいいと思います。
         * 処理速度としては、ifelse文で書き続ける場合よりも早くなります。
         * もちろん、switch文である必要もあるのかも考える必要があるのですが
         */
        if (requestCode == REQUEST_PERMISSION) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cameraIntent();
                /* ここでの return は不要 */
                return;

            } else {
                // それでも拒否された時の対応
                Toast toast = Toast.makeText(this, "実行できません", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
}