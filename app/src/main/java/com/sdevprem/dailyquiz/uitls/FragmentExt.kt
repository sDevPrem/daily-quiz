package com.sdevprem.dailyquiz.uitls

import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun Fragment.launchInLifecycle(toDo : suspend CoroutineScope.() -> Unit){
    viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED){
            toDo()
        }
    }
}

fun Fragment.toast(msg : String){
    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}