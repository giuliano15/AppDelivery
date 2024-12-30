package com.example.project1732.Activity;

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.project1732.R
import com.example.project1732.via_cep.data.local.entity.AddressEntity
import com.example.project1732.via_cep.domain.model.Address
import com.example.project1732.via_cep.presenter.search.SearchAddressViewModel
import com.example.project1732.via_cep.util.OnAddressEditedListener
import com.example.project1732.via_cep.util.StateView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditarEnderecoBottomSheetFragment : BottomSheetDialogFragment() {

    private var address: Address? = null
    private var listener: OnAddressEditedListener? = null
    private val viewModel: SearchAddressViewModel by viewModels()


    companion object {
        private const val ARG_ADDRESS = "address"

        fun newInstance(
            address: Address,
            listener: OnAddressEditedListener
        ): EditarEnderecoBottomSheetFragment {
            val fragment = EditarEnderecoBottomSheetFragment()
            val args = Bundle()
            args.putParcelable(ARG_ADDRESS, address) // Use putParcelable para passar o Address
            fragment.arguments = args
            fragment.listener = listener
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate o layout do BottomSheet
        return inflater.inflate(R.layout.dialog_editar_endereco, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        val bottomSheet =
            dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as? FrameLayout
        bottomSheet?.let {
            // Ajusta os layoutParams diretamente para definir uma altura específica
            val layoutParams = it.layoutParams
            layoutParams.height =
                800 // Defina a altura desejada em pixels (aqui 1000px, ajuste conforme necessário)
            it.layoutParams = layoutParams

            // Forçar o BottomSheet a se expandir
            val behavior = BottomSheetBehavior.from(it)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true // Evita que o BottomSheet fique colapsado
            behavior.isDraggable = true // Permite arrastar o BottomSheet
        }

        return dialog
    }

    override fun onStart() {
        super.onStart()

        val bottomSheet =
            dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as? FrameLayout
        bottomSheet?.let {
            // Ajusta os layoutParams diretamente para definir uma altura específica
            val layoutParams = it.layoutParams
            layoutParams.height =
                800 // Defina a altura desejada em pixels (aqui 1000px, ajuste conforme necessário)
            it.layoutParams = layoutParams

            // Forçar o BottomSheet a se expandir
            val behavior = BottomSheetBehavior.from(it)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true // Evita que o BottomSheet fique colapsado
            behavior.isDraggable = true // Permite arrastar o BottomSheet
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        address = arguments?.getParcelable(ARG_ADDRESS) // Obter o endereço passado
        val btnExcuirEndereco: Button = view.findViewById(R.id.btnExcuirEndereco)
        val editTextEndereco: EditText = view.findViewById(R.id.editTextEnderco)
        editTextEndereco.setText(address?.getFullAddress()) // Usar o método para obter o endereço formatado

        val btnSalvarEndereco: Button = view.findViewById(R.id.btnSalvarEndereco)

        btnExcuirEndereco.setOnClickListener {
            address?.let { address ->
                listener?.onAddressDeleted(address) // Chama o listener de exclusão
                dismiss() // Fecha o BottomSheet
            }
        }

        // Preencher o EditText com o endereço atual, se houver
        editTextEndereco.setText(address?.getFullAddress())

        btnSalvarEndereco.setOnClickListener {
            // Obter o texto do EditText
            val fullAddressText = editTextEndereco.text.toString().trim()

            if (fullAddressText.isNotEmpty()) {
                // Decompor o endereço em partes e criar um novo objeto Address
                val updatedAddress = parseFullAddress(fullAddressText, address)

                if (updatedAddress != null) {
                    // Salvar o endereço atualizado no banco de dados
                    saveAddressToDatabase(updatedAddress)
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Por favor, insira um endereço válido",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    // Função para decompor o endereço completo em suas partes e atualizar o objeto Address
    private fun parseFullAddress(fullAddress: String, originalAddress: Address?): Address? {
        // Divida o endereço completo com base no padrão que você usa (ex: separação por " - ")
        val parts = fullAddress.split(" - ")

        if (parts.size < 4) {
            Toast.makeText(
                requireContext(),
                "Endereço incompleto -> O endereço deve ter rRua - Bairro - cidade - Estado(MG)",
                Toast.LENGTH_SHORT
            ).show()
            return null
        }

        // Retorne um novo objeto Address com as partes divididas
        return originalAddress?.copy(
            logradouro = parts[0],
            bairro = parts[1],
            localidade = parts[2],
            uf = parts[3],
            cep = ""
        )
    }

    private fun saveAddressToDatabase(address: Address) {
        // Converta para AddressEntity
        val addressEntity = AddressEntity(
            id = address.id,
            cep = "",
            logradouro = address.logradouro,
            bairro = address.bairro,
            localidade = address.localidade,
            uf = address.uf
        )

        // Salve no banco de dados usando o ViewModel
        viewModel.updateAddress(addressEntity).observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.Loading -> {
                    // Exibir um loader ou algum feedback para o usuário
                }

                is StateView.Success -> {
                    // Notifique que o endereço foi alterado
                    listener?.onAddressEdited(address) // Notifica o endereço atualizado
                    Toast.makeText(
                        requireContext(),
                        "Endereço atualizado com sucesso!",
                        Toast.LENGTH_SHORT
                    ).show()
                    dismiss() // Fecha o BottomSheet
                }

                is StateView.Error -> {
                    Toast.makeText(
                        requireContext(),
                        "Erro ao atualizar endereço",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}


