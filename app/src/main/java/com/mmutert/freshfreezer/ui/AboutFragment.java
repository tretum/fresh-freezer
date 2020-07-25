package com.mmutert.freshfreezer.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mmutert.freshfreezer.BuildConfig;
import com.mmutert.freshfreezer.R;
import com.mmutert.freshfreezer.databinding.FragmentAboutBinding;

import java.net.URL;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AboutFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AboutFragment extends Fragment {


    private FragmentAboutBinding mBinding;


    public AboutFragment() {
        // Required empty public constructor
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AboutFragment.
     */
    public static AboutFragment newInstance() {

        AboutFragment fragment = new AboutFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_about, container, false);

        mBinding.tvAboutVersion.setText(getString(R.string.fragment_about_version_text, BuildConfig.VERSION_NAME));

        setUpGithubButton();
        setUpFeedbackButton();
        setUpBugReportButton();

        return mBinding.getRoot();
    }


    private void setUpBugReportButton() {

        mBinding.tvAboutReportBug.setOnClickListener(v -> {
            String githubURL = getString(R.string.github_repository_url) + "/issues";
            Uri uri = Uri.parse(githubURL);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            getActivity().startActivity(intent);
        });
    }


    private void setUpFeedbackButton() {

        mBinding.tvAboutFeedback.setOnClickListener(v -> {
            Uri uri = Uri.fromParts(
                    "mailto",
                    "Alex <" + getString(R.string.support_email) + ">",
                    null
            );
            Intent intent = new Intent(Intent.ACTION_SENDTO, uri)
                    .putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_email_subject));
            getActivity().startActivity(intent);
        });
    }


    private void setUpGithubButton() {

        mBinding.tvAboutGithub.setOnClickListener(v -> {
            String githubURL = getString(R.string.github_repository_url);
            Uri uri = Uri.parse(githubURL);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            getActivity().startActivity(intent);
        });
    }
}