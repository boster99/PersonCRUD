package com.ctoddcook.android.personcrud;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

/**
 * This is the adapter used by ViewDetailActivity. This is the connector between the array of
 * PersonModel objects and the one-at-a-time ViewPager in activity_view_detail_frame.xml. It uses
 * the class PersonDetailFragment, which handles populating the display with data for a single
 * PersonModel object.
 * <p>
 * Created by C. Todd Cook on 9/20/2016.<br>
 * ctodd@ctoddcook.com
 */
class PersonPagerAdapter extends FragmentStatePagerAdapter {
    private final ArrayList<PersonModel> mPersonList;

    /**
     * Constructor passes a FragmentManager up the chain to a parent class, and stores a reference
     * to the list of Fuelings to be displayed.
     *
     * @param fragmentManager the manager overseeing this adapter
     * @param personList      a list of fuelings, from which details will be retrieved for display
     */
    PersonPagerAdapter(FragmentManager fragmentManager, ArrayList<PersonModel> personList) {
        super(fragmentManager);
        mPersonList = personList;
    }

    /**
     * Return the number of views available.
     */
    @Override
    public int getCount() {
        return mPersonList.size();
    }

    /**
     * Return the Fragment associated with a specified position.
     *
     * @param position the position of the row that is to be displayed
     */
    @Override
    public Fragment getItem(int position) {
        if (position >= 0 && position < mPersonList.size())
            return PersonDetailFragment.getInstance(mPersonList.get(position).getID());

        return null;
    }
}
