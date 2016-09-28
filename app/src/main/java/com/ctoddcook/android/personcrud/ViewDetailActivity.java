package com.ctoddcook.android.personcrud;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;


/**
 * This class works with PersonPagerAdapter to provide one-at-a-time display of PersonModel
 * objects.
 * <p/>
 * Created by C. Todd Cook on 9/18/2016.<br>
 * ctodd@ctoddcook.com
 */
public class ViewDetailActivity extends AppCompatActivity {
    public static final String ARG_POSITION = "Position";

    private ViewPager mPager;


    /**
     * Setup work when the activity is created. Gathers incoming details about the record type and
     * list position to be displayed, and passes them to the method which handles the display.
     *
     * @param savedInstanceState Information about the state of the activity if it was stopped
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_detail_frame);

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPager = (ViewPager) findViewById(R.id.Details_Pager);

        int position = getIntent().getIntExtra(ARG_POSITION, -1);

        showFragment(position);
    }

    /**
     * Displays the fragment for viewing details of a Person.
     *
     * @param position The position in the list of records to display first
     */
    private void showFragment(int position) {
        PersonPagerAdapter personPagerAdapter = new PersonPagerAdapter(getSupportFragmentManager(),
            PersistenceHandler.getInstance().fetchPersonList());
        mPager.setAdapter(personPagerAdapter);
        mPager.setCurrentItem(position);
        setTitle(R.string.title_view_person);
    }
}
