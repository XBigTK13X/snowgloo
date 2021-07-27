package com.simplepathstudios.snowgloo;

import android.os.Bundle;
import android.view.Menu;

import com.google.android.gms.cast.framework.CastContext;

interface IMainActivity {
    void setActionBarSubtitle(String subtitle);

    CastContext getCastContext();

    void setActionBarTitle(String title);
}
