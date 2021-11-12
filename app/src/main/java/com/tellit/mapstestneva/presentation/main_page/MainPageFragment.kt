package com.tellit.mapstestneva.presentation.main_page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.tellit.mapstestneva.databinding.FragmentMainPageBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainPageFragment : Fragment(), OnMapReadyCallback {
    lateinit var binding: FragmentMainPageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onMapReady(p0: GoogleMap) {
        TODO("Not yet implemented")
    }
}



