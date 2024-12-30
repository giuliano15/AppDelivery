package com.example.project1732.via_cep.presenter.list

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels

import androidx.navigation.fragment.findNavController
import com.example.project1732.Activity.Principal
import com.example.project1732.R
import com.example.project1732.databinding.FragmentListAddressBinding
import com.example.project1732.via_cep.domain.model.Address
import com.example.project1732.via_cep.presenter.list.adapter.AddressAdapter
import com.example.project1732.via_cep.presenter.list.adapter.VerticalSpaceItemDecoration
import com.example.project1732.via_cep.util.StateView


import dagger.hilt.android.AndroidEntryPoint

class ListAddressFragment : Fragment() {

    private val viewModel: ListAddressViewModel by activityViewModels()

    private var _binding: FragmentListAddressBinding? = null
    private val binding get() = _binding!!

    private lateinit var addressAdapter: AddressAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListAddressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecycler()

        initObservers()

        initListeners()

        back()
    }

    private fun back() {
        binding.backBtnEnd.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun initListeners() {
        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_listAddressFragment_to_searchAddressFragment)
        }
    }

    private fun initObservers() {
        viewModel.addressList.observe(viewLifecycleOwner) { addresses ->
            addressAdapter.submitList(addresses)

        }

        viewModel.addressChanged.observe(viewLifecycleOwner) {
            viewModel.getAllAddress()
        }

    }

    private fun initRecycler() {
        // Obtenha o nome do usuário de onde ele estiver armazenado
        val userName = viewModel.getUserName() // Exemplo, pegue o nome do usuário do ViewModel ou de outra fonte


        addressAdapter = AddressAdapter(userName, { fullAddress ->
            // Ao clicar no item, navegue ou faça alguma ação
            val intent = Intent(requireActivity(), Principal::class.java)
            intent.putExtra("FULL_ADDRESS", fullAddress)
            startActivity(intent)
        }, { address ->
            // Ao clicar longo no item, chamar a função de deletar no ViewModel
            showDeleteConfirmationDialog(address) // Aqui você chama o diálogo diretamente
        }, requireContext(), { address ->
            // Aqui você passa a referência do diálogo
            showDeleteConfirmationDialog(address)
        })

        val verticalSpaceItemDecoration = VerticalSpaceItemDecoration(
            verticalSpaceHeight = 8, // Margem entre os itens
            firstItemTopMargin = 16, // Margem top para o primeiro item
            lastItemBottomMargin = 16 // Margem bottom para o último item
        )

        with(binding.recyclerAddress) {
            adapter = addressAdapter
            addItemDecoration(verticalSpaceItemDecoration)
        }
    }

    private fun showDeleteConfirmationDialog(address: Address) {
        // Exibe um diálogo para confirmar a exclusão
        AlertDialog.Builder(requireContext())
            .setTitle("Excluir Endereço")
            .setMessage("Tem certeza de que deseja excluir este endereço?")
            .setPositiveButton("Sim") { _, _ ->
                // Chama a função de deletar no ViewModel
                viewModel.deleteAddress(address).observe(viewLifecycleOwner) { stateView ->
                    when (stateView) {
                        is StateView.Loading -> {
                            // Exibe um loading se necessário
                        }
                        is StateView.Success -> {
                            // Atualiza a lista de endereços ou exibe uma mensagem de sucesso
                            viewModel.getAllAddress() // Atualiza a lista após deletar
                        }
                        is StateView.Error -> {
                            // Exibe uma mensagem de erro
                            Toast.makeText(
                                requireContext(),
                                "Erro ao deletar o endereço",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
            .setNegativeButton("Não", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
