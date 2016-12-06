package tab.list;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thunder.missile.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jeremy on 2016/6/28.
 */
public class FileTab_music extends Fragment {

    /*
 * 由ftab來管理f_系列的class，f_bmusic是用來顯示幕前手機中所有的音樂，其它都與f_image相同
 */
    private static final String ARG_SECTION_NUMBER = "section_number";
    AttachParameter music = new AttachParameter();
    ListView Audiolist;
    View v;
    public static FileTab_music newInstance(int sectionNumber) {
        FileTab_music fragment = new FileTab_music();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.tab_list, container, false);
        Uri uri_music;
        getActivity().getIntent().setData(Uri.parse("content://tab.list.d2d/file_choice"));
        final Uri uri_test = getActivity().getIntent().getData();
        music.seturi(uri_test);

        final String[] columns = { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.SIZE, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ALBUM_ID, };
        final String orderBy = MediaStore.Audio.Media._ID;
        boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if(sdCardExist){
            uri_music = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }else{
            uri_music = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
        }
        final Cursor musiccursor = getActivity().getContentResolver().query(uri_music, columns, null, null, orderBy);

        final int music_column_index = musiccursor.getColumnIndex(MediaStore.Audio.Media._ID);
        music.setcount(musiccursor.getCount());
        music.setarrPath(new String[music.getcount()]);
        music.setPath(new String[music.getcount()]);
        music.setarrName(new String[music.getcount()]);
        music.setthumbnails(new Bitmap[music.getcount()]);
        music.setthumbnailsselection(new boolean[music.getcount()]);
        Thread thread = new Thread() {
            @Override
            public void run () {
                for (int i = 0; i < music.getcount(); i++) {
                    musiccursor.moveToPosition(i);
                    int id = musiccursor.getInt(music_column_index);
                    int dataColumnIndex = musiccursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                    int FileColumnIndex = musiccursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
                    music.arrPath[i] = musiccursor.getString(dataColumnIndex);
                    music.Path[i] = String.valueOf(i);
                    music.arrName[i] = musiccursor.getString(FileColumnIndex);

                }
                musiccursor.close();
            }
        };
        thread.run();


        Audiolist = (ListView) v.findViewById(R.id.PhoneVideoList);
        Audiolist.setTextFilterEnabled(true);
        Audiolist.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        Audiolist.setBackgroundResource(R.drawable.background_v1);
        Audiolist.setCacheColorHint(Color.TRANSPARENT);

        Audiolist.setAdapter(new ImageAdapter());
        // autoComplete = (AutoCompleteTextView)
        // v.findViewById(R.id.autoCompleteTextView1);
        // ArrayAdapter<String> adapter = new
        // ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,arrPath);
        // autoComplete.setAdapter(adapter);
        return v;
    }
    public class ImageAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        public ImageAdapter() {
            mInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return music.getcount();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.tab_music, null);
                holder.fileNameView = (TextView) convertView.findViewById(R.id.textView1);
                holder.checkbox = (CheckBox) convertView.findViewById(R.id.itemCheckBox1);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.checkbox.setId(position);
            holder.fileNameView.setId(position);
            holder.checkbox.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    String[] Form = { FileContentProvider.UserSchema._ID };
                    CheckBox cb = (CheckBox) v;
                    int id = cb.getId();
                    Pattern patternphoto = Pattern.compile("file_name");
                    Matcher matchorther = patternphoto.matcher(music.arrPath[id]);
                    if (music.thumbnailsselection[id]) {
                        cb.setChecked(false);
                        Cursor c = getActivity().getContentResolver().query(music.geturi(), Form, "path_id='" + music.Path[id] + "'", null, null);
                        c.moveToFirst();
                        music.setid(Integer.parseInt(c.getString(0)));
                        //2016/08/04 music.id_this改成 music.getid()
                        String where = FileContentProvider.UserSchema._ID + " = " + music.getid();
                        getActivity().getContentResolver().delete(music.geturi(), where, null);
                        music.thumbnailsselection[id] = false;
                    } else {
                        Cursor ch_temp_cursor = getActivity().getContentResolver().query(music.geturi(), Form, null, null, null);
                        if (ch_temp_cursor.getCount() > 0) {
                            Toast.makeText(getActivity().getApplicationContext(), "最多只能選一個檔案", Toast.LENGTH_SHORT).show();
                            cb.setChecked(false);
                            ch_temp_cursor.close();
                        } else if (matchorther.find()) {
                            Toast.makeText(getActivity().getApplicationContext(), "抱歉，不能選此檔案，此檔與系統衝突", Toast.LENGTH_SHORT).show();
                            cb.setChecked(false);
                            ch_temp_cursor.close();
                        } else {
                            ch_temp_cursor.close();
                            cb.setChecked(true);
                            ContentValues values = new ContentValues();
                            values.put(FileContentProvider.UserSchema._FILEPATH, music.arrPath[id]);
                            values.put(FileContentProvider.UserSchema._PATHID, music.Path[id]);
                            getActivity().getContentResolver().insert(music.geturi(), values);
                            music.thumbnailsselection[id] = true;
                        }

                    }
                }

            });
            holder.fileNameView.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    // TODO Auto-generated method stub

                    int id = v.getId();
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    String tt=music.arrPath[id];
                    intent.setDataAndType(Uri.parse("file://" + tt), "audio/*");
                    startActivity(intent);
                }
            });
            holder.fileNameView.setText(music.arrName[position]);
            holder.checkbox.setChecked(music.thumbnailsselection[position]);
            holder.id = position;
            return convertView;
        }
    }

    class ViewHolder {
        TextView fileNameView;
        CheckBox checkbox;
        int id;
    }
}
