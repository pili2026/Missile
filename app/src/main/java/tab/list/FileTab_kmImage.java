package tab.list;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.thunder.missile.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Jeremy on 2016/9/18.
 */

public class FileTab_kmImage extends Fragment {

    AttachParameter image = new AttachParameter();
    View v;

    public FileTab_kmImage() {
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.tab_gallery, container, false);
        getActivity().getIntent().setData(Uri.parse("content://tab.list.d2d/file_choice"));
        final Uri uri_test = getActivity().getIntent().getData();
        image.seturi(uri_test);

        final String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME };
        final String orderBy = MediaStore.Images.Media._ID;

        // 這邊的DB式取得手機內建的圖片table，query第一個是tabel的連結、第二個是要抓出什麼欄位出來使用、第三設定null表示沒有設定條件，第四個是指排序的方式
        Cursor imageCursor = getActivity().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy);
		/*
		 * getColumnIndex的方法是，請告訴我MediaStore.Images.Media._ID在眾多的欄位裡， 它的位置是第幾個
		 */
        int image_column_index = imageCursor.getColumnIndex(MediaStore.Images.Media._ID);
        image.setcount(imageCursor.getCount());
        // 這邊是紀錄檔案的總數量
        image.setthumbnails(new Bitmap[image.getcount()]);
        image.setarrPath(new String[image.getcount()]);
        image.setarrName(new String[image.getcount()]);
        image.setthumbnailsselection(new boolean[image.getcount()]);
        // 這裡是針對每個檔案儲存各自的資訊
        for (int i = 0; i < image.getcount(); i++) {
            // 必須將指標(像滑鼠)移到所要執行的資料，如沒有使用,則會發生找不到資料的錯誤
            imageCursor.moveToPosition(i);
            int id = imageCursor.getInt(image_column_index);
            // 抓出MediaStore.Images.Media.DATA的欄位在哪一個位置
            int dataColumnIndex = imageCursor.getColumnIndex(MediaStore.Images.Media.DATA);
            // 抓出MediaStore.Images.Media.DISPLAY_NAME的欄位在哪一個位置
            int FileColumnIndex = imageCursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
            // 取得圖片的縮圖
            image.thumbnails[i] = MediaStore.Images.Thumbnails.getThumbnail(getActivity().getApplicationContext().getContentResolver(), id, MediaStore.Images.Thumbnails.MICRO_KIND, null);
			/*
			 * 剛據剛抓出來的位置，使用getString就可以得到該位置的值
			 * 例如FileColumnIndex抓出來是1，透過getString可以得到 檔案名稱
			 */
            image.arrPath[i] = imageCursor.getString(dataColumnIndex);
            image.arrName[i] = imageCursor.getString(FileColumnIndex);
            // .println("arrName[id]="+arrName[id]);
        }
        imageCursor.close();
        GridView imagegrId = (GridView) v.findViewById(R.id.PhoneImageGrid);
        // 使用自訂義的adapter
        imagegrId.setAdapter(new ImageAdapter());

        return v;
    }

    public class ImageAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        public ImageAdapter() {
            mInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return image.count;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
			/*
			 * 使用holder的目的是管理每一個checkbox的狀態。
			 * 因為不管是listview+checkbox，或者是gridview+checkbox
			 * 當資料筆數大於頁面可所顯示的大小時，會有所謂的回收機制，但因為不加上checkbox所以看不出來
			 * 若當加上checkbox時並進行點擊時，會造成checkbox跳的問題產生，所以holder式解決checkbox亂跳的問題之一
			 */
            FileTab_kmImage.ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.tab_image, null);
                holder.imageview = (ImageView) convertView.findViewById(R.id.thumbImage);
                holder.checkbox = (CheckBox) convertView.findViewById(R.id.itemCheckBox);

                convertView.setTag(holder);
            } else {
                holder = (FileTab_kmImage.ViewHolder) convertView.getTag();
            }
            holder.checkbox.setId(position);
            holder.imageview.setId(position);
            holder.checkbox.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    String[] Form = { FileContentProvider.UserSchema._ID };
                    CheckBox cb = (CheckBox) v;
                    int id = cb.getId();

                    Pattern patternphoto = Pattern.compile("file_name");
                    Matcher matchorther = patternphoto.matcher(image.arrPath[id]);

                    if (image.thumbnailsselection[id]) {
                        cb.setChecked(false);
                        // 這裡的DB是當使用者取消所選擇的檔案時，要從table中刪除當初所選中的檔案
                        Cursor c = getActivity().getContentResolver().query(image.geturi(), Form, "filepath='" + image.arrPath[id] + "'", null, null);
                        c.moveToFirst();
                        image.setid(Integer.parseInt(c.getString(0)));
                        String where = FileContentProvider.UserSchema._ID + " = " + image.getid();
                        getActivity().getContentResolver().delete(image.geturi(), where, null);
                        c.close();
                        image.thumbnailsselection[id] = false;
                    } else {
                        // 先檢查是否已經選擇過檔案了
                        Cursor ch_temp_cursor = getActivity().getContentResolver().query(image.geturi(), Form, null, null, null);
                        if (ch_temp_cursor.getCount() > 0) {
                            Toast.makeText(getActivity().getApplicationContext(), "最多只能選一個檔案", Toast.LENGTH_SHORT).show();
                            cb.setChecked(false);
                            ch_temp_cursor.close();
                        } else if (matchorther.find()) {
                            Toast.makeText(getActivity().getApplicationContext(), "抱歉，不能選此檔案，此檔與系統衝突", Toast.LENGTH_SHORT).show();
                            cb.setChecked(false);
                            ch_temp_cursor.close();
                        } else {
                            // 把檔案的路徑加入到TABLE內
                            cb.setChecked(true);
                            ContentValues values = new ContentValues();
                            values.put(FileContentProvider.UserSchema._FILEPATH, image.arrPath[id]);
                            getActivity().getContentResolver().insert(image.geturi(), values);
                            image.thumbnailsselection[id] = true;
                        }

                    }
                }
            });
            holder.imageview.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    int id = v.getId();
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse("file://" + image.arrPath[id]), "image/*");
                    startActivity(intent);
                }
            });
            holder.imageview.setImageBitmap(image.thumbnails[position]);
            holder.checkbox.setChecked(image.thumbnailsselection[position]);
            holder.id = position;
            return convertView;
        }
    }

    class ViewHolder {
        ImageView imageview;
        CheckBox checkbox;
        int id;
    }

}
