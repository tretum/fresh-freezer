package com.mmutert.freshfreezer.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.mmutert.freshfreezer.BuildConfig
import com.mmutert.freshfreezer.R
import com.mmutert.freshfreezer.databinding.FragmentAboutBinding

/**
 * A simple [Fragment] subclass.
 * Use the [AboutFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AboutFragment : Fragment() {
    private lateinit var mBinding: FragmentAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_about, container, false)
        mBinding.tvAboutVersion.text = getString(R.string.fragment_about_version_text, BuildConfig.VERSION_NAME)

        setUpGithubButton()
        setUpFeedbackButton()
        setUpBugReportButton()

        return mBinding.root
    }

    private fun setUpBugReportButton() {
        mBinding.tvAboutReportBug.setOnClickListener {
            val githubURL = getString(R.string.github_repository_url) + "/issues"
            val uri = Uri.parse(githubURL)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            requireActivity().startActivity(intent)
        }
    }

    private fun setUpFeedbackButton() {
        mBinding.tvAboutFeedback.setOnClickListener {
            val uri = Uri.fromParts(
                    "mailto",
                    "Alex <" + getString(R.string.support_email) + ">",
                    null
            )
            val intent = Intent(Intent.ACTION_SENDTO, uri)
                    .putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_email_subject))
            requireActivity().startActivity(intent)
        }
    }

    private fun setUpGithubButton() {
        mBinding.tvAboutGithub.setOnClickListener {
            val githubURL = getString(R.string.github_repository_url)
            val uri = Uri.parse(githubURL)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            requireActivity().startActivity(intent)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment AboutFragment.
         */
        fun newInstance(): AboutFragment {
            val fragment = AboutFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}