package mapotempo.com.mapotempo_fleet_android.mission;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.mapotempo.fleet.api.MapotempoFleetManagerInterface;
import com.mapotempo.fleet.api.model.MissionInterface;
import com.mapotempo.fleet.api.model.accessor.MissionAccessInterface;
import com.mapotempo.fleet.core.model.Mission;

import java.util.List;

import mapotempo.com.mapotempo_fleet_android.MapotempoApplication;
import mapotempo.com.mapotempo_fleet_android.R;

/**
 * This fragment act as a manger for the {@link MissionDetailsFragment}.
 * It is used to :
 * <ul>
 *     <li>Detect if a ViewPager must be used</li>
 *     <li>Return the current view displayed</li>
 *     <li>Provide the {@link Mission} object to {@link MissionDetailsFragment}</li>
 * </ul>
 *
 * <h3>Integration</h3>
 * First and foremost, it is needed to implement the fragment through XML using the following class :
 * {@literal <fragment class="mapotempo.com.mapotempo_fleet_android.mission.MissionFragment"} <br>
 * {@literal android:id="@+id/base_fragment"} <br>
 * {@literal app:ViewStyle="SCROLLVIEW"} <br>
 * {@literal android:layout_width="match_parent"} <br>
 * {@literal android:layout_height="match_parent" />} <br>
 * <p>
 * It is Highly recommended to set up the enum "ViewStyle" to "SCROLLVIEW" in order to benefit of full features.
 *
 * This fragment require the implementation of {@link ContainerFragmentMission} directly in the Activity that hold the List Fragment.
 * Then Override the {@link ContainerFragmentMission#wichViewIsTheCurrent(int)} is required to use this fragment.
 * </p>
 *
 * <b>Here is an example of usability: </b>
 * <pre>
 * @Override
 * public int wichViewIsTheCurrent(int position) {
 *      MissionsFragment missionsFragment = (MissionsFragment) getSupportFragmentManager().findFragmentById(R.id.listMission);
 *
 *      if (missionsFragment != null)
 *      missionsFragment.setCurrentMission(position);
 *
 *      return position;
 *  }
 * </pre>
 */
public class MissionFragment extends Fragment {
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private MissionDetailsFragment fMission;
    private List<MissionInterface> mMissions;
    private MapotempoFleetManagerInterface mManager;
    private int mCount;
    private ContainerFragmentMission mListener;

    public enum ViewStyle {
        VIEWSCROLL(0),
        SIMPLEVIEW(1);

        private int mValue;

        ViewStyle(int value) {
            mValue = value;
        }

        public static ViewStyle fromInt(int value) {
            switch (value) {
                case 0:
                    return VIEWSCROLL;
                case 1:
                default:
                    return SIMPLEVIEW;
            }
        }
    }

    private ViewStyle mViewStyle = ViewStyle.SIMPLEVIEW;

    public MissionFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getManagerAndMissions();
    }

    private void getManagerAndMissions() {
        MapotempoApplication mapotempoApplication = (MapotempoApplication) getActivity().getApplicationContext();
        mManager = mapotempoApplication.getManager();
        if (mManager == null)
            return;

        MissionAccessInterface missionAccessInterface = mManager.getMissionAccess();
        mMissions = missionAccessInterface.getAll();
        mCount = mMissions.size();
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);

        TypedArray attrCustom = context.obtainStyledAttributes(attrs, R.styleable.MissionFragment);
        Integer v = attrCustom.getInteger(R.styleable.MissionFragment_ViewStyle, 1);
        mViewStyle = ViewStyle.fromInt(v);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_base, container, false);
        LinearLayout content = view.findViewById(R.id.mission_view_content);

        if (mViewStyle == ViewStyle.VIEWSCROLL) {
            mPagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager(), mCount, mMissions);
            ViewPager viewPager = (ViewPager) getActivity().getLayoutInflater().inflate(R.layout.view_pager, null);
            mPager = viewPager.findViewById(R.id.mission_viewpager);

            mPager.setPageTransformer(true, new DepthPageTransformer());
            setPagerChangeListener();
            content.addView(viewPager);
            mPager.setAdapter(mPagerAdapter);
            setListenerForButtons(view);
            setNextAndPreviousVisibility(view);
        } else {
            FragmentManager manager = getFragmentManager();
            FragmentTransaction fragmentTransaction = manager.beginTransaction();

            fMission = new MissionDetailsFragment();
            fragmentTransaction.add(R.id.mission_view_content, fMission);
            fragmentTransaction.commit();
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (ContainerFragmentMission) context;
        if (mListener == null)
            throw new RuntimeException("You must implement ContainerFragmentMission Interface");
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mListener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mPager = null;
        mPagerAdapter = null;
        fMission = null;
    }

    public void notifyDataChange() {
        mPagerAdapter.notifyDataSetChanged();
    }

    public void refreshPagerData(List<MissionInterface> missions) {
        mMissions = missions;
        ScreenSlidePagerAdapter screenSlidePagerAdapter = (ScreenSlidePagerAdapter) mPagerAdapter;

        if (screenSlidePagerAdapter != null)
            screenSlidePagerAdapter.updateMissions(missions);
    }

    public void setCurrentItem(int position) {
        if (mViewStyle == ViewStyle.VIEWSCROLL) {
            mPager.setCurrentItem(position, true);
        } else {
            fMission.fillViewFromActivity();
        }
    }

    private void setPagerChangeListener() {
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {
                mListener.wichViewIsTheCurrent(position);
                setNextAndPreviousVisibility(getView());
            }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });

        final int position = getActivity().getIntent().getIntExtra("mission_id", 0);
        mPager.post(new Runnable() {
            @Override
            public void run() {
                mPager.setCurrentItem(position);
            }
        });
    }

    private void setNextAndPreviousVisibility(View view) {
        int currentItem = mPager.getCurrentItem();
        final Button next = view.findViewById(R.id.next_nav);
        final Button prev = view.findViewById(R.id.previous_nav);

        next.setVisibility(View.VISIBLE);
        prev.setVisibility(View.VISIBLE);

        if(currentItem == mPagerAdapter.getCount() - 1) {
            next.setVisibility(View.INVISIBLE);
        } else if (currentItem == 0) {
            prev.setVisibility(View.INVISIBLE);
        }
    }

    private void setListenerForButtons(View view) {
        final Button prev = view.findViewById(R.id.previous_nav);
        final Button next = view.findViewById(R.id.next_nav);

        if (prev != null && next != null) {
            prev.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int currentItem = mPager.getCurrentItem();
                    mPager.setCurrentItem(currentItem - 1);
                }
            });

            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int currentItem = mPager.getCurrentItem();
                    mPager.setCurrentItem(currentItem + 1);
                }
            });
        } else {
            throw new RuntimeException("Button aren't settled in view");
        }
    }

    public interface ContainerFragmentMission {
        int wichViewIsTheCurrent(int page);
    }
}