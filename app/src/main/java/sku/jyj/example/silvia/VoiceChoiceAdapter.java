package sku.jyj.example.silvia;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

public class VoiceChoiceAdapter extends RecyclerView.Adapter<VoiceChoiceAdapter.ViewHolder> {

    private List<File> voiceFiles;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(File file);
        void onDeleteClick(File file);
        void onPlayClick(File file);
    }

    public VoiceChoiceAdapter(Context context, List<File> voiceFiles, OnItemClickListener listener) {
        this.context = context;
        this.voiceFiles = voiceFiles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.voicechoice_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File voiceFile = voiceFiles.get(position);
        holder.voiceName.setText(voiceFile.getName());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일");
        String date = sdf.format(voiceFile.lastModified());
        holder.voiceDate.setText("등록 날짜 : " + date);

        holder.btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onPlayClick(voiceFile);
            }
        });

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDeleteClick(voiceFile);
            }
        });

        holder.btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(voiceFile);
            }
        });
    }

    @Override
    public int getItemCount() {
        return voiceFiles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView voiceName, voiceDate;
        Button btnPlay, btnDelete, btnSelect;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            voiceName = itemView.findViewById(R.id.voice_name);
            voiceDate = itemView.findViewById(R.id.voice_date);
            btnPlay = itemView.findViewById(R.id.button13); // 재생 버튼
            btnDelete = itemView.findViewById(R.id.voice_delete); // 삭제 버튼
            btnSelect = itemView.findViewById(R.id.button_select); // 선택하기 버튼
        }
    }
}
