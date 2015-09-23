package jp.ac.u_tokyo.t.utdroid_camera;

import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    /* Viewを格納する変数 */
    private CameraPreview cameraPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* CameraPreviewのインスタントを生成 */
        cameraPreview = new CameraPreview(this);

        /* Android 4.4移行であれば、以下のコードでナビゲーションバーを非表示にできる */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View decorView = this.getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

        /* レイアウトのxmlファイルを読み込む代わりにCameraPreviewを指定 */
        setContentView(cameraPreview);
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
