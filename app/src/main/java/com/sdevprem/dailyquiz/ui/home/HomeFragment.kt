package com.sdevprem.dailyquiz.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Timestamp
import com.sdevprem.dailyquiz.R
import com.sdevprem.dailyquiz.data.model.Quiz
import com.sdevprem.dailyquiz.data.repository.QuizRepository
import com.sdevprem.dailyquiz.data.repository.UserRepository
import com.sdevprem.dailyquiz.data.util.Response
import com.sdevprem.dailyquiz.data.util.filter.QuizFilter
import com.sdevprem.dailyquiz.databinding.DialogDateFilterBinding
import com.sdevprem.dailyquiz.databinding.FragmentHomeBinding
import com.sdevprem.dailyquiz.uitls.DateUtils
import com.sdevprem.dailyquiz.uitls.DateUtils.getStringMth
import com.sdevprem.dailyquiz.uitls.DateUtils.setMaximumTimeOfMth
import com.sdevprem.dailyquiz.uitls.DateUtils.setMinimumTimeOfMth
import com.sdevprem.dailyquiz.uitls.DateUtils.toCalendar
import com.sdevprem.dailyquiz.uitls.launchInLifecycle
import com.sdevprem.dailyquiz.uitls.toast
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {
    lateinit var binding: FragmentHomeBinding
    private val adapter = QuizAdapter(emptyList()) {
        findNavController().navigate(
            HomeFragmentDirections.actionHomeFragmentToQuestionNav(quizList[it].id)
        )
    }
    private val viewModel: HomeVM by viewModels()

    private val dateFilterBinding by lazy {
        DialogDateFilterBinding.inflate(
            LayoutInflater.from(requireContext())
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentHomeBinding
            .inflate(
                inflater,
                container,
                false
            ).apply { binding = this }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val configuration = AppBarConfiguration(
            setOf(R.id.homeFragment),//top level destinations
            binding.drawerLayout
        )
        binding.toolbar.setupWithNavController(findNavController(), configuration)
        binding.drawerNavigation.setNavigationItemSelectedListener { menuItem ->
            if (menuItem.itemId == R.id.logout) {
                lifecycleScope.launch {
                    viewModel.logOut()
                        .collectLatest {
                            if (it)
                                findNavController().navigate(
                                    HomeFragmentDirections.actionHomeFragmentToAppIntroFragment()
                                )
                            else toast("Oops! Something went wrong please try again")
                        }
                }
                return@setNavigationItemSelectedListener true
            } else return@setNavigationItemSelectedListener false
        }

        binding.quizGrid.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.quizGrid.adapter = adapter

        launchInLifecycle {
            viewModel.user.collectLatest {
                when (it) {
                    is Response.Success -> binding.apply {
                        var score = 0
                        it.data.attemptedQuizzes.forEach { quizScore ->
                            score += quizScore.value.score
                        }
                        drawerNavigation.getHeaderView(0)
                            ?.findViewById<TextView>(R.id.user_total_score)
                            ?.text = getString(R.string.user_quiz_result_score, score)
                    }

                    is Response.Error -> toast("Something went wrong. Please try again later")
                    else -> {}
                }
            }
        }
        launchInLifecycle {
            viewModel.quiz.collectLatest {
                when (it) {
                    is Response.Success -> binding.apply {
                        progressBar.isVisible = false
                        quizGrid.isVisible = true
                        adapter.quizList = it.data
                    }

                    is Response.Loading -> binding.apply {
                        quizGrid.isVisible = false
                        progressBar.isVisible = true
                    }

                    is Response.Error -> toast("Something went wrong. Please try again later")
                    else -> {}
                }
            }
        }
        binding.dateFilter.setOnClickListener {
            dateFilterBinding.apply {
                year.setText(viewModel.getFilterYear().toString())
                mth.setText(viewModel.getFilterMthString())
                (root.parent as? ViewGroup)?.removeView(root)
            }
            val dialog = MaterialAlertDialogBuilder(requireContext())
                .setView(dateFilterBinding.root)
                .setTitle("Choose year and month")
                .setPositiveButton("Filter") { dialog, _ ->
                    if (verifyDateFilter(
                            dateFilterBinding.year.text.toString(),
                            dateFilterBinding.mth.text.toString()
                        )
                    )
                        viewModel.setDate(
                            dateFilterBinding.year.text.toString().toInt(),
                            DateUtils.convertStringMthToCalendarMth(
                                dateFilterBinding.mth.text.toString(),
                                requireContext()
                            )
                        )
                    else {
                        dialog.dismiss()
                    }
                }
                .setNegativeButton("Cancel") { _, _ -> }
                .create()
            dialog.show()
            dialog.apply {
                (requireView().parent as ViewGroup)
                    .apply {
                        setPadding(requireView().paddingLeft, 0, requireView().paddingRight, 0)
                    }
            }
        }
    }

    private fun verifyDateFilter(year: String, mth: String): Boolean {
        try {
            if (
                year.length < 4 ||
                !(year.toInt() >= 2000 && year.toInt() <= Calendar.getInstance().get(Calendar.YEAR))
            ) {
                toast("Enter valid year")
                return false
            } else if (DateUtils.convertStringMthToCalendarMth(mth, requireContext()) < 0) {
                toast("Choose valid month")
                return false
            }
        } catch (e: Exception) {
            toast("Enter valid data")
            return false
        }
        return true
    }

    fun getList() = adapter.quizList

    private fun populateDummyData() {
        val dummyList = buildList<Quiz>(30) {
            for (i in 1..30)
                add(Quiz(timestamp = Timestamp.now()))
        }
        adapter.quizList = dummyList
    }
}

@HiltViewModel
class HomeVM @Inject constructor(
    quizRepository: QuizRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val quizFilter = MutableStateFlow(QuizFilter())

    @OptIn(ExperimentalCoroutinesApi::class)
    val quiz = quizFilter.flatMapLatest {
        quizRepository.getQuizList(it)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(10_000),
        Response.Loading
    )

    val user = userRepository.getUser()
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            Response.Loading
        )


    fun logOut() = userRepository.logout()
        .catch { emit(false) }

    fun setDate(year: Int, calendarMth: Int) {
        val date = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, calendarMth)
        }

        //if the date is equal to the previous one
        //then don't apply filter
        quizFilter.value.apply {
            toDate.toCalendar().apply {
                if (get(Calendar.YEAR) == year && get(Calendar.MONTH) == calendarMth)
                    return@setDate
            }
        }

        quizFilter.value = QuizFilter(
            fromDate = date.setMinimumTimeOfMth().time,
            toDate = date.setMaximumTimeOfMth().time
        )
    }

    fun getFilterMthString() = quizFilter.value.fromDate.toCalendar().getStringMth()
    fun getFilterYear() = quizFilter.value.fromDate.toCalendar().get(Calendar.YEAR)
}