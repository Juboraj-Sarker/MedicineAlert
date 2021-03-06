package com.juborajsarker.medicare.fragment;


import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.juborajsarker.medicare.R;
import com.juborajsarker.medicare.activity.appointments.AddAppointmentActivity;
import com.juborajsarker.medicare.adapter.AppointmentAdapter;
import com.juborajsarker.medicare.database.AppointmentDatabase;
import com.juborajsarker.medicare.dataparser.DateCalculations;
import com.juborajsarker.medicare.dataparser.GridSpacingItemDecoration;
import com.juborajsarker.medicare.model.AppointmentModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarView;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;


public class AppointmentFragment extends Fragment {

    View view;
    InterstitialAd mInterstitialAd;

    Calendar startDate;
    Calendar endDate;


    HorizontalCalendar horizontalCalendar;
    ImageView leftIV, rightIV;
    TextView dateTV, appointmentTV;
    FloatingActionButton fabAdd;
    RecyclerView recyclerView;

    AppointmentAdapter adapter;
    List<AppointmentModel> appointmentModelList;
    AppointmentDatabase database;
    AppointmentModel model;


    public AppointmentFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_appointment, container, false);

        mInterstitialAd = new InterstitialAd(getContext());
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_full_screen1));


        init();
        setupCalender();

        return view;
    }

    private void showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    private void init() {

        Date currentDate = Calendar.getInstance().getTime();
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.UK);
        String searchQuery = df.format(currentDate);

        leftIV = (ImageView) view.findViewById(R.id.leftIV);
        rightIV = (ImageView) view.findViewById(R.id.rightIV);
        dateTV = (TextView) view.findViewById(R.id.dateTV);
        appointmentTV = (TextView) view.findViewById(R.id.appointment_TV);
        recyclerView = (RecyclerView) view.findViewById(R.id.appointment_RV);
        fabAdd = (FloatingActionButton) view.findViewById(R.id.fab_add);



        leftIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                setupCurrentDate();

            }
        });

        rightIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setupCurrentDate();

            }
        });

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AdRequest adRequest = new AdRequest.Builder().addTestDevice("93448558CC721EBAD8FAAE5DA52596D3").build();
                mInterstitialAd.loadAd(adRequest);

                mInterstitialAd.setAdListener(new AdListener() {
                    public void onAdLoaded() {
                        showInterstitial();
                    }
                });

                startActivity(new Intent(getContext(), AddAppointmentActivity.class));
            }
        });


        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM - yyyy");
        String formattedCurrentDate = sdf.format(currentDate);

        dateTV.setText("Today " +formattedCurrentDate );

        database = new AppointmentDatabase(getContext());
        appointmentModelList = new ArrayList<>();
        model = new AppointmentModel();


        prepareForView(searchQuery);


    }




    private void prepareForView(String searchQuery) {

        appointmentModelList.clear();
        appointmentModelList = database.getSelectedAppointment(searchQuery);

        if (appointmentModelList.size() > 0){

            appointmentTV.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

        }else if (appointmentModelList.size() == 0){

            appointmentTV.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }

        RecyclerView.LayoutManager layoutManagerBeforeMeal = new GridLayoutManager(getContext(), 1);
        recyclerView.setLayoutManager(layoutManagerBeforeMeal);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(1, dpToPx(2), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());




        adapter = new AppointmentAdapter(getContext(), appointmentModelList, getActivity(), recyclerView, adapter, searchQuery);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }


    private void setupCurrentDate() {


        horizontalCalendar.goToday(true);


    }

    private void setupCalender() {

        startDate = Calendar.getInstance();
        startDate.add(Calendar.MONTH, -3);

        endDate = Calendar.getInstance();
        endDate.add(Calendar.MONTH, 3);




        horizontalCalendar = new HorizontalCalendar.Builder(view, R.id.calendarView)
                .range(startDate, endDate)
                .datesNumberOnScreen(7)
                .configure()
                .formatTopText("EEE")
                .showTopText(true)
                .showBottomText(false)
                .formatBottomText("MMM-yy")
                .end()
                .build();







        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {

                Date currentDate = date.getTime();
                DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.UK);
                String searchQuery = df.format(currentDate);

                imageViewIssue(date, position);
                setTextViewDate(date);
                prepareForView(searchQuery);


            }

            @Override
            public void onCalendarScroll(HorizontalCalendarView calendarView, int dx, int dy) {

            }

            @Override
            public boolean onDateLongClicked(Calendar date, int position) {
                return true;
            }
        });

    }


    private void imageViewIssue(Calendar date, int position) {

        Date selectedDate = date.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM - yyyy");
        String formattedDate = sdf.format(selectedDate);

        String result = new DateCalculations().compareDate(formattedDate);

        if (result.equals("big")){

            leftIV.setVisibility(View.VISIBLE);
            rightIV.setVisibility(View.GONE);


        }else if (result.equals("small")){

            rightIV.setVisibility(View.VISIBLE);
            leftIV.setVisibility(View.GONE);

        }

    }


    private void setTextViewDate(Calendar date) {

        Date currentDate = Calendar.getInstance().getTime();
        Date myDate = date.getTime();

        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM - yyyy");
        SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM - yyyy");

        String formattedCurrentDate = sdf.format(currentDate);
        String formattedDate = df.format(myDate);

        if (formattedCurrentDate.equals(formattedDate)){

            dateTV.setText("Today " + formattedCurrentDate );

            leftIV.setVisibility(View.GONE);
            rightIV.setVisibility(View.GONE);

        }else {



            String addDate = new DateCalculations().addForHorizontalCalender(formattedCurrentDate, "1");
            String subDate = new DateCalculations().subForHorizontalCalender(formattedCurrentDate, "1");

            if (addDate.equals(formattedDate)){


                dateTV.setText("Tomorrow " + formattedDate);

            }else if (subDate.equals(formattedDate)){



                dateTV.setText("Yesterday " + formattedDate );

            }else {

                dateTV.setText(formattedDate );
            }




        }

    }


    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

}
