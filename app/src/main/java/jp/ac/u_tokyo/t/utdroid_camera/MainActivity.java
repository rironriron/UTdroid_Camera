package jp.ac.u_tokyo.t.utdroid_camera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    /* Viewを格納する変数 */
    private CameraPreview cameraPreview;

    /* Permission取得のための定数 */
    private static final int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Android 4.4以降であれば、以下のコードでナビゲーションバーを非表示にできる */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View decorView = this.getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

        /* Android 6.0以上かどうかで条件分岐 */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            /* Permissionを取得済みかどうか確認 */
            String[] dangerousPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                /* 未取得ならPermissionを要求 */
                requestPermissions(dangerousPermissions, PERMISSION_REQUEST_CODE);
            }else{
                setupUI();
            }
        }else{
            setupUI();
        }
    }

    /*
     * Android 6.0以上のDANGEROUS_PERMISSION対策
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length < 2) {
                /* キャンセルされた場合はアプリを終了する */
                Toast.makeText(this, "カメラとSDカードへのアクセスを許可して下さい。", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }else if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                /* Permissionが許可された */
                setupUI();
            } else {
                /* Permissionが許可されなかった */
                Toast.makeText(this, "カメラとSDカードへのアクセスを許可して下さい。", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void setupUI() {
        /* CameraPreviewのインスタントを生成 */
        cameraPreview = new CameraPreview(this);

        /* レイアウトのxmlファイルを読み込む代わりにCameraPreviewを指定 */
        setContentView(cameraPreview);

        /* レイアウトのxmlファイルから読み込むことも出来る */
        /* setContentView(R.layout.activity_main); */
    }

    /* 物理キーでシャッターを切る */
    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        switch (e.getKeyCode()) {
            /* 音量キーのみに反応 */
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (e.getAction() == KeyEvent.ACTION_DOWN) {
                    /* CameraPreviewをクリックしてシャッターを作動させる
                       第4引数と第5引数でタップした位置を指定できる（今回はどこでも良いが） */
                    MotionEvent event = MotionEvent.obtain(SystemClock.uptimeMillis(),
                            SystemClock.uptimeMillis() + 100,
                            MotionEvent.ACTION_DOWN, 100f, 100f, 0);
                    cameraPreview.onTouchEvent(event);
                    return true;
                }
        }
        return super.dispatchKeyEvent(e);
    }
}
