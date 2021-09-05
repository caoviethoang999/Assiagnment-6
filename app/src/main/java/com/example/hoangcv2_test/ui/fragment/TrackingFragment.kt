package com.example.hoangcv2_test.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.hoangcv2_test.R
import com.example.hoangcv2_test.database.Run
import com.example.hoangcv2_test.other.Constrants.ACTION_PAUSE_SERVICE
import com.example.hoangcv2_test.other.Constrants.ACTION_START_OR_RESUME_SERVICE
import com.example.hoangcv2_test.other.Constrants.ACTION_STOP_SERVICE
import com.example.hoangcv2_test.other.Constrants.MAP_ZOOM
import com.example.hoangcv2_test.other.Constrants.POLYLINE_COLOR
import com.example.hoangcv2_test.other.Constrants.POLYLINE_WIDTH
import com.example.hoangcv2_test.other.TrackingUtility
import com.example.hoangcv2_test.services.Polyline
import com.example.hoangcv2_test.services.TrackingServices
import com.example.hoangcv2_test.viewmodel.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*
import kotlin.math.round
import java.util.*
import javax.inject.Inject


const val CANCEL_TRACKING_DIALOG_TAG="cancel"

@AndroidEntryPoint
class TrackingFragment:Fragment(R.layout.fragment_tracking) {
    private val viewModel: MainViewModel by viewModels()
    private var isTracking=false
    private var pathPoints = mutableListOf<Polyline>()
    private var map: GoogleMap?=null

    private var curTimeInMillis = 0L
    private var menu: Menu?=null

    @set:Inject
    private var weight=80f


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)
        btnToggleRun.setOnClickListener {
            toggleRun()
        }
        //save data when rotate
        if(savedInstanceState !=null){
            //call the CancelTrackingDialog
            val cancelTrackingDiaglog=parentFragmentManager.findFragmentByTag(
                CANCEL_TRACKING_DIALOG_TAG
            )as CancelTrackingDialog?
            cancelTrackingDiaglog?.setYesListenr {
                stopRun()
            }
        }
        btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSaveToDb()
        }
        mapView.getMapAsync {
            map=it
            addAllPolyLines()
        }
        subscribeToObservers()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu,menu)
        this.menu=menu

    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if(curTimeInMillis>0L){
            this.menu?.getItem(0)?.isVisible=true
        }
    }
    private fun showCancelTrackingDialog(){
        CancelTrackingDialog().apply {
            setYesListenr {
                stopRun()
            }
        }.show(parentFragmentManager,CANCEL_TRACKING_DIALOG_TAG)
    }
    private fun stopRun(){
        tvTimer.text="00:00:00"
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.miCancelTracking->{
                showCancelTrackingDialog()
            }
        }
        return super.onOptionsItemSelected(item)

    }
    private fun subscribeToObservers(){
        TrackingServices.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })
        TrackingServices.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints=it
            addLastestPolyline()
            moveCameraToUser()
        })
        TrackingServices.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            curTimeInMillis=it
            val formattedTime=TrackingUtility.getFormattedStopWatchTime(curTimeInMillis,true)
            tvTimer.text=formattedTime
        })
    }
    private fun toggleRun(){
        if(isTracking){
            menu?.getItem(0)?.isVisible=true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        }else{
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }
    private fun updateTracking(isTracking:Boolean){
        this.isTracking=isTracking
        if(!isTracking && curTimeInMillis > 0L){
            btnToggleRun.text="Start"
            btnFinishRun.visibility=View.VISIBLE
        }else if(isTracking){
            menu?.getItem(0)?.isVisible=true
            btnToggleRun.text="Stop"
            btnFinishRun.visibility=View.GONE
        }
    }
    private fun moveCameraToUser(){
        if(pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()){
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }
    private fun zoomToSeeWholeTrack(){
        val bounds= LatLngBounds.Builder()
        for(polyline in pathPoints){
            for(pos in polyline){
                bounds.include(pos)
            }
        }
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                mapView.width,
                mapView.height,
                (mapView.height * 0.05f).toInt()
            )
        )
    }

    private fun endRunAndSaveToDb(){
        map?.snapshot { bmp ->
            var distanceInMeters=0
            for(polyline in pathPoints){
                distanceInMeters +=TrackingUtility.calculatePolylineLenght(polyline).toInt()

            }
            val avgSpeed=round((distanceInMeters/1000f)/(curTimeInMillis/1000f/60/60) *10)/10f
            val dateTimestamp=Calendar.getInstance().timeInMillis
            val caloriesBurned=((distanceInMeters /1000f)* weight).toInt()
            val run=Run(bmp,dateTimestamp,avgSpeed,distanceInMeters,curTimeInMillis,caloriesBurned)
            viewModel.insertRun(run)
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "Run saved successfully",
                Snackbar.LENGTH_LONG
            ).show()
            stopRun()
        }
    }
    //creating polyline on map
    private fun addAllPolyLines(){
        for(polyline in pathPoints){
            val polylineOptions=PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }
    //get last known polyline location and current polyline location
    private fun addLastestPolyline(){
        if(pathPoints.isNotEmpty() && pathPoints.last().size >1){
            val preLastLatLng=pathPoints.last()[pathPoints.last().size -2]
            val lastLatLng=pathPoints.last().last()
            val polylineOption=PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            map?.addPolyline(polylineOption)
        }
    }

    //send request to start service
    private fun sendCommandToService(action:String)=
        Intent(requireContext(),TrackingServices::class.java).also{
        it.action=action
        requireContext().startService(it)
    }
    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }
}