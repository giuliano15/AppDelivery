package com.example.project1732.via_cep.presenter.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.Toast

import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.project1732.R
import com.example.project1732.databinding.FragmentSearchAddressBinding
import com.example.project1732.via_cep.domain.model.Address
import com.example.project1732.via_cep.presenter.list.ListAddressViewModel
import com.example.project1732.via_cep.util.StateView
import com.example.project1732.via_cep.util.hideKeyboard



import dagger.hilt.android.AndroidEntryPoint
import eightbitlab.com.blurview.RenderScriptBlur

@AndroidEntryPoint
class SearchAddressFragment : Fragment() {

    private val viewModel: SearchAddressViewModel by viewModels()
    private val listAddressViewModel: ListAddressViewModel by activityViewModels()

    private var _binding: FragmentSearchAddressBinding? = null
    private val binding get() = _binding!!

    private var address: Address? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchAddressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListeners()
        setupBlurEffect()
        back()
    }

    private fun back() {
        binding.backBtnSearch.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupBlurEffect() {
        // Obtém a referência para a BlurView
        val decorView = requireActivity().window.decorView
        val rootView = decorView.findViewById<ViewGroup>(android.R.id.content)
        val windowBackground = decorView.background

        // Configura a BlurView com o rootView e o background
        binding.blurView.setupWith(rootView, RenderScriptBlur(requireContext()))
            .setFrameClearDrawable(windowBackground) // Define o background
            .setBlurRadius(10f) // Define o raio do desfoque

        // Define as propriedades de contorno da BlurView
        binding.blurView.setOutlineProvider(ViewOutlineProvider.BACKGROUND)
        binding.blurView.clipToOutline = true
        binding.itemAddress.icMenu.visibility = View.GONE
    }


    private fun initListeners() {
        binding.editCep.addTextChangedListener {
            val text = it?.toString() ?: ""
            if (text.isNotEmpty()) {
                if (text.length == 8) {
                    hideKeyboard()
                    getAddress(cep = text)
                }
            }
        }

        binding.btnSalve.setOnClickListener {
            if (address != null) {
                insertAddress(address!!)
            } else {
                findNavController().popBackStack()
            }
        }
    }

    private fun getAddress(cep: String) {
        viewModel.getAddress(cep).observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.Loading -> {
                    binding.btnSalve.isEnabled = false
                    binding.itemAddress.viewFlipper.displayedChild = 2
                }
                is StateView.Success -> {
                    if (stateView.data?.cep != null) {
                        address = stateView.data

                        binding.btnSalve.isEnabled = true
                        binding.itemAddress.viewFlipper.displayedChild = 1
                        binding.itemAddress.textAddress.text = stateView.data.getFullAddress()
                    } else {
                        binding.btnSalve.isEnabled = false
                        binding.itemAddress.viewFlipper.displayedChild = 0
                        binding.itemAddress.textEmptyAddress.text =
                            getString(R.string.label_address_empty_search_address_fragment)
                    }
                }
                is StateView.Error -> {
                    binding.btnSalve.isEnabled = false
                    binding.itemAddress.viewFlipper.displayedChild = 0
                    binding.itemAddress.textEmptyAddress.text =
                        getString(R.string.label_address_empty_search_address_fragment)
                }
            }
        }
    }

    private fun insertAddress(address: Address) {
        viewModel.insertAddress(address).observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.Loading -> {

                }
                is StateView.Success -> {
                    listAddressViewModel.addressChanged()
                    findNavController().popBackStack()
                }
                is StateView.Error -> {
                    Toast.makeText(requireContext(), "Erro ao salvar endereço", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}