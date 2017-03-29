package id.sch.smktelkom_mlg.project2.xirpl511203031.prayertimes.list;

import android.view.View;

public abstract class ItemNotification {
    private View view;

    public ItemNotification(View view) {
        this.setView(view);
    }

    public abstract void show();

    public abstract void hide();

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }
}
