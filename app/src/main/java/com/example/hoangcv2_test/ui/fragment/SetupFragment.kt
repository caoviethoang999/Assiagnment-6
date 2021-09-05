package com.example.hoangcv2_test.ui.fragment

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.hoangcv2_test.R
import com.example.hoangcv2_test.other.Constrants.KEY_FIRST_TIME_TOGGLE
import com.example.hoangcv2_test.other.Constrants.KEY_NAME
import com.example.hoangcv2_test.other.Constrants.KEY_WEIGHT
import com.example.hoangcv2_test.viewmodel.MainViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_setup.*
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment:Fragment(R.layout.fragment_setup) {

    @Inject
    lateinit var sharedPref: SharedPreferences

    //use for data type
    @set:Inject
    var isFirstAppOpen=true


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(!isFirstAppOpen){
            //nav initiate to setupFragment then run toward to runFragment
            val navOptions= NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment,true)
                .build()
            findNavController().navigate(
                R.id.action_setupFragment_to_runFragment,
                savedInstanceState,
                navOptions
            )
        }
        tvContinue.setOnClickListener {
            val success= writePersonalDataToSharedPref()
            if(success) {
                //make nav run from setupFragment to runFragment
                findNavController().navigate(R.id.action_setupFragment_to_runFragment)
            }else{
                //equal to Toast
                Snackbar.make(requireView(),"Please enter all the fields",Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun writePersonalDataToSharedPref():Boolean{
        val name= etName.text.toString()
        val weight=etWeight.text.toString()
        if(name.isEmpty()|| weight.isEmpty()){
            return false
        }
        sharedPref.edit()
            .putString(KEY_NAME,name)
            .putFloat(KEY_WEIGHT,weight.toFloat())
            .putBoolean(KEY_FIRST_TIME_TOGGLE,false)
            .apply()
        val toolbarText ="Let s go, $name!"
        requireActivity().tvToolbarTitle.text=toolbarText
        return true
    }
}