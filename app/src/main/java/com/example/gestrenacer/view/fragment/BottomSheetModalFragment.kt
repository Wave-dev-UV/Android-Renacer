package com.example.gestrenacer.view.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.gestrenacer.databinding.FragmentBottomSheetModalBinding
import com.example.gestrenacer.viewmodel.SharedViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetModalFragment : BottomSheetDialogFragment() {

    private val requestCameraPermission = 100
    private var _binding: FragmentBottomSheetModalBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBottomSheetModalBinding.inflate(
            inflater,
            container,
            false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val btnGallery = binding.btnGaleria
        val btnCamera = binding.cameraButton
        val btnDelete = binding.btnDelete

        btnGallery.setOnClickListener { sharedViewModel.selectGallery(); dismiss() }
        btnCamera.setOnClickListener { selectCamera(); dismiss() }
        btnDelete.setOnClickListener { sharedViewModel.selectDelete(); dismiss() }
    }

    private fun selectCamera(){
        if (isCameraPermissions()) return sharedViewModel.selectCamera()

        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.CAMERA),
            requestCameraPermission
        )
    }

    private fun isCameraPermissions(): Boolean{
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()

        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val behavior = BottomSheetBehavior.from(bottomSheet!!)

        behavior.isDraggable = true
    }

}