package img.enc;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import java.io.*;
import java.util.Random;

public class MainActivity extends Activity {
    static final int REQ = 1000;
    static final String MARK = "ENCv1";
    ImageView iv1, iv2;
    EditText et;
    TextView tv;
    Bitmap bm;
    String ext;
    boolean isEnc;
    String imgInfo;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        iv1 = findViewById(R.id.iv_original);
        iv2 = findViewById(R.id.iv_processed);
        et = findViewById(R.id.et_password);
        tv = findViewById(R.id.tv_status);
        findViewById(R.id.btn_select_image).setOnClickListener(v -> pick());
        et.setOnEditorActionListener((v, a, e) -> {
            if (a == EditorInfo.IME_ACTION_DONE || (e != null && e.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                proc();
                return true;
            }
            return false;
        });
    }

    void pick() {
        startActivityForResult(new Intent(Intent.ACTION_PICK).setType("image/*"), REQ);
    }

    @Override
    protected void onActivityResult(int r, int c, Intent d) {
        super.onActivityResult(r, c, d);
        if (r == REQ && c == RESULT_OK && d != null) {
            try {
                byte[] data = readAll(getContentResolver().openInputStream(d.getData()));
                isEnc = data.length > 5 && MARK.equals(new String(data, data.length - 5, 5));
                bm = BitmapFactory.decodeByteArray(data, 0, isEnc ? data.length - 5 : data.length);
                iv1.setImageBitmap(bm);
                ext = getExt(d.getData());
                imgInfo = bm.getWidth() + "x" + bm.getHeight();
                tv.setText((isEnc ? "已加密，回车解密" : "未加密，回车加密") + " | " + imgInfo);
            } catch (Exception e) {
                toast("读取失败");
            }
        }
    }

    byte[] readAll(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = is.read(buf)) != -1) baos.write(buf, 0, n);
        is.close();
        return baos.toByteArray();
    }

    void proc() {
        if (bm == null || et.getText().toString().trim().isEmpty()) {
            toast(bm == null ? "请先选图" : "请输入密码");
            return;
        }
        new Thread(() -> {
            long t = System.currentTimeMillis();
            Bitmap r = process(bm, et.getText().toString().trim());
            runOnUiThread(() -> {
                iv2.setImageBitmap(r);
                tv.setText((isEnc ? "解密" : "加密") + "成功(" + (System.currentTimeMillis() - t) + "ms)" + " | " + imgInfo);
                save(r, System.currentTimeMillis() + (isEnc ? "_dec" : "_enc") + "." + ext, !isEnc);
            });
        }).start();
    }

    Bitmap process(Bitmap b, String p) {
        int w = b.getWidth(), h = b.getHeight(), n = w * h;
        Bitmap r = b.copy(Bitmap.Config.ARGB_8888, true);
        int[] px = new int[n], out = new int[n], idx = new int[n];
        r.getPixels(px, 0, w, 0, 0, w, h);
        for (int i = 0; i < n; i++) idx[i] = i;
        long seed = 0;
        for (char c : p.toCharArray()) seed = seed * 31 + c;
        Random rnd = new Random(seed);
        for (int i = n - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1), tmp = idx[i];
            idx[i] = idx[j];
            idx[j] = tmp;
        }
        if (isEnc) for (int i = 0; i < n; i++) out[idx[i]] = px[i];
        else for (int i = 0; i < n; i++) out[i] = px[idx[i]];
        r.setPixels(out, 0, w, 0, 0, w, h);
        return r;
    }

    String getExt(Uri u) {
        String n = u.getLastPathSegment();
        return n != null && n.contains(".") ? n.substring(n.lastIndexOf(".") + 1) : "png";
    }

    void save(Bitmap b, String n, boolean enc) {
        try {
            File f = new File(Environment.getExternalStorageDirectory() + "/Download", n);
            f.getParentFile().mkdirs();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            b.compress(n.endsWith("jpg") || n.endsWith("jpeg") ? Bitmap.CompressFormat.JPEG : Bitmap.CompressFormat.PNG, 100, baos);
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(baos.toByteArray());
            if (enc) fos.write(MARK.getBytes());
            fos.close();
            toast("已保存:" + f.getPath());
        } catch (Exception e) {
            toast("保存失败");
        }
    }

    void toast(String s) {
        runOnUiThread(() -> Toast.makeText(this, s, Toast.LENGTH_SHORT).show());
    }
}
