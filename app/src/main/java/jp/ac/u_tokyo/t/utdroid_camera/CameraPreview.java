package jp.ac.u_tokyo.t.utdroid_camera;

import android.content.Context;
import android.hardware.Camera;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * カメラのファインダーを表示する独自のView
 * SurfaceViewは通常のViewよりも高fpsでの描画に適している
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    /* カメラを操るための変数 */
    private Camera camera;

    /* 連射を防止するためのフラグ */
    private boolean underProcessing = false;

    /* コンテクスト情報を保持するための変数 */
    private Context context;

    /* コンストラクタ */
    public CameraPreview(Context context) {
        super(context);

        this.context = context;

        /* SurfaceHolderのコールバックを登録 */
        getHolder().addCallback(this);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    /* レイアウトxmlに埋め込む時に使われるコンストラクタ */
    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;

        /* SurfaceHolderのコールバックを登録 */
        getHolder().addCallback(this);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    /**
     * Surfaceが作成された時に呼ばれるメソッド
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        /* カメラを起動する */
        if (camera.getNumberOfCameras() > 0) {
            /* 端末に複数のカメラが搭載されている場合でも、大抵は0番がメインカメラ */
            camera = Camera.open(0);
            try {
                /* カメラのプレビュー画像をholderに流し込むよう指定 */
                camera.setPreviewDisplay(holder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Surfaceが変化した時に呼ばれるメソッド
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (camera != null) {
            /* カメラのパラメータを取得 */
            Camera.Parameters params = camera.getParameters();

            /* 撮影サイズの調整（最高画質で撮影） */
            List<Camera.Size> pictureSizes = params.getSupportedPictureSizes();
            int maxHeight = 0;
            for (Camera.Size size : pictureSizes) {
                if (size.height < maxHeight) {
                    maxHeight = size.height;
                    params.setPictureSize(size.width, size.height);
                }
            }

            /* プレビューサイズの調整（アスペクト比が近いほど歪みが少ない） */
            List<Camera.Size> preSizes = params.getSupportedPreviewSizes();
            float minError = Float.MAX_VALUE;
            for (Camera.Size size : preSizes) {
                float error = Math.abs(1.0f * size.width / size.height - 1.0f * width / height);
                if (error < minError) {
                    minError = error;
                    params.setPreviewSize(size.width, size.height);
                }
            }

            /* プレビュー開始 */
            camera.startPreview();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (camera != null) {
            /* プレビューを停止 */
            camera.stopPreview();
            /* カメラを解放する */
            camera.release();
            camera = null;
        }
    }

    /**
     * Viewがタッチされた時に呼ばれるメソッド
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /* 指を離した瞬間に反応する（タッチした瞬間より手ブレが少ない） */
        if (event.getAction() == MotionEvent.ACTION_UP) {
            /* 処理中でなければオートフォーカスを作動させ、連射防止のフラグを立てる */
            if (camera != null && underProcessing == false) {
                underProcessing = true;
                camera.autoFocus(autoFocusListener);
            }
        }
        return true;
    }

    /**
     * オートフォーカス完了のコールバック
     */
    private Camera.AutoFocusCallback autoFocusListener = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, final Camera camera) {
            /* シャッターを切る */
            camera.takePicture(shutterListener, null, null, jpegListener);
        }
    };

    /*
     * シャッターを切った時のコールバック
     */
    private Camera.ShutterCallback shutterListener = new Camera.ShutterCallback() {
        public void onShutter() {
            /* 何もしない（シャッター音は自動で鳴る） */
        }
    };

    /**
     * 撮影データが取得可能になった時のコールバック
     */
    private Camera.PictureCallback jpegListener = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            if (data == null) {
                /* データがない場合 */
                Toast.makeText(context, "撮影データを取得できませんでした。", Toast.LENGTH_SHORT).show();
            } else {
                /* SDカードの存在をチェック */
                if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    Toast.makeText(context, "SDカードを利用できません。", Toast.LENGTH_SHORT).show();
                } else {
                    /* 保存するディレクトリを */
                    String filePath = Environment.getExternalStorageDirectory().getPath() + "/UTdroid/";
                    File directory = new File(filePath);

                    /* ディレクトリが無ければ作る */
                    if (!directory.exists()) {
                        directory.mkdir();
                    }

                    /* 現在時刻からファイル名を生成 */
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                    Date date = new Date(System.currentTimeMillis());
                    String fileName = sdf.format(date) + ".jpg";

                    try {
                        /* データの書き込み処理 */
                        FileOutputStream fos = new FileOutputStream(filePath + fileName);
                        BufferedOutputStream bos = new BufferedOutputStream(fos);
                        bos.write(data);
                        bos.close();
                    } catch (Exception e) {
                        Toast.makeText(context, "ファイルの保存中にエラーが発生しました。", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            }

            /* 連射防止フラグを元に戻す */
            underProcessing = false;

            /* ファインダーを再開 */
            camera.startPreview();
        }
    };
}
