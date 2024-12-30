package com.example.project1732.via_cep.presenter.list.adapter
//
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.project1732.Activity.EditarEnderecoBottomSheetFragment
import com.example.project1732.databinding.ItemAddressBinding
import com.example.project1732.via_cep.data.local.entity.AddressEntity
import com.example.project1732.via_cep.domain.model.Address
import com.example.project1732.via_cep.util.OnAddressEditedListener
import com.google.firebase.database.FirebaseDatabase
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur

//class AddressAdapter(
//    private val userName: String, // Nome do usuário para operações no Firebase
//    private val onItemClick: (String) -> Unit, // Ação para editar
//    private val onItemLongClick: (Address) -> Unit, // Ação para deletar
//    private val context: Context
//) : ListAdapter<Address, AddressAdapter.ViewHolder>(DIFF_CALLBACK) {
//
//    companion object {
//        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Address>() {
//            override fun areItemsTheSame(oldItem: Address, newItem: Address): Boolean {
//                return oldItem.cep == newItem.cep
//            }
//
//            override fun areContentsTheSame(oldItem: Address, newItem: Address): Boolean {
//                return oldItem == newItem
//            }
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        return ViewHolder(
//            ItemAddressBinding.inflate(
//                LayoutInflater.from(parent.context),
//                parent,
//                false
//            )
//        )
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val address = getItem(position)
//        holder.bind(address) // Vincular os dados do endereço
//    }
//
//    // ViewHolder interno para controlar os itens do RecyclerView
//    inner class ViewHolder(private val binding: ItemAddressBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//
//        // Função para vincular os dados do endereço e configurar eventos
//        fun bind(address: Address) {
//            // Configurar o endereço completo no TextView
//            binding.textAddress.text = address.getFullAddress()
//
//            // Definir clique simples no item
//            binding.root.setOnClickListener {
//                // Salva o endereço clicado no Firebase
//                saveAddressToFirebase(userName, address.getFullAddress())
//                // Chama a função de clique definida no Adapter (pode ser usada para editar)
//                onItemClick(address.getFullAddress())
//            }
//
//            // Configurar o botão de menu de overflow
//            binding.icMenu.setOnClickListener {
//                showPopupMenu(address)
//            }
//
//            // Definir o viewFlipper se necessário (ajustar de acordo com seu layout)
//            binding.viewFlipper.displayedChild = 1
//        }
//
//        // Função para exibir o popup menu
//        private fun showPopupMenu(address: Address) {
//            val popupMenu = PopupMenu(context, binding.icMenu)
//            popupMenu.inflate(com.example.project1732.R.menu.menu_overflow)
//            popupMenu.setOnMenuItemClickListener { menuItem ->
//                when (menuItem.itemId) {
//                    com.example.project1732.R.id.action_excluir -> {
//                        deleteItem(address) // Chama a função de deletar o item
//                        true
//                    }
//                    com.example.project1732.R.id.action_editar -> {
//                        editItem(address) // Chama a função de editar o item
//                        true
//                    }
//                    else -> false
//                }
//            }
//            popupMenu.show()
//        }
//
//        // Função para excluir um item do Firebase
//        private fun deleteItem(address: Address) {
//            FirebaseDatabase.getInstance()
//                .getReference("users")
//                .child(userName)
//                .child("formulario")
//                .child("endereco")
//                .setValue(null) // Remove o endereço no Firebase
//                .addOnCompleteListener {
//                    if (it.isSuccessful) {
//                        // Chama o callback de exclusão
//                        onItemLongClick(address)
//                        //Toast.makeText(context, "Endereço excluído com sucesso!", Toast.LENGTH_SHORT).show()
//                    } else {
//                        Toast.makeText(context, "Erro ao excluir endereço", Toast.LENGTH_SHORT).show()
//                    }
//                }
//        }
//
//        // Função para editar um item (personalize de acordo com sua lógica de edição)
//        private fun editItem(address: Address) {
//            onItemClick(address.getFullAddress()) // Aqui você pode abrir um dialog ou outra tela para editar o endereço
//        }
//    }
//
//    // Função para salvar o endereço no Firebase
//    private fun saveAddressToFirebase(userName: String, fullAddress: String) {
//        FirebaseDatabase.getInstance()
//            .getReference("users")
//            .child(userName)
//            .child("formulario")
//            .child("endereco")
//            .setValue(fullAddress)
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    Toast.makeText(context, "Endereço salvo com sucesso!", Toast.LENGTH_SHORT).show()
//                } else {
//                    Toast.makeText(context, "Erro ao salvar endereço no Firebase", Toast.LENGTH_SHORT).show()
//                }
//            }
//    }
//}

class AddressAdapter(
    private val userName: String, // Adiciona o nome do usuário
    private val onItemClick: (String) -> Unit,
    private val onItemLongClick: (Address) -> Unit,
    private val context: Context,
    private val showDeleteDialog: (Address) -> Unit

) : ListAdapter<Address, AddressAdapter.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Address>() {
            override fun areItemsTheSame(oldItem: Address, newItem: Address): Boolean {
                return oldItem.cep == newItem.cep
            }

            override fun areContentsTheSame(oldItem: Address, newItem: Address): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemAddressBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val address = getItem(position)

        holder.binding.icMenu.setOnClickListener {
            val bottomSheet = EditarEnderecoBottomSheetFragment.newInstance(
                address, // Passa o objeto Address em vez de uma String
                object : OnAddressEditedListener {
                    override fun onAddressEdited(updatedAddress: Address) {
                        updateItem(position, updatedAddress) // Atualiza a lista com o novo endereço
                    }

                    override fun onAddressDeleted(deletedAddress: Address) {

                        showDeleteDialog(deletedAddress)
                        //deleteAddressFromFirebase(userName, deletedAddress.getFullAddress())
                        //removeItem(position) // Remover o item da lista
                    }
                }
            )
            bottomSheet.show((context as AppCompatActivity).supportFragmentManager, bottomSheet.tag)
        }




//        holder.binding.icMenu.setOnClickListener{
//
//            val bottomSheet = EditarEnderecoBottomSheetFragment.newInstance(address.getFullAddress())
//            bottomSheet.show((context as AppCompatActivity).supportFragmentManager, bottomSheet.tag)
//        }

        holder.binding.textAddress.text = address.getFullAddress()

        // Definindo o clique no item
        holder.itemView.setOnClickListener {
            // Salva o endereço clicado no Firebase
            saveAddressToFirebase(userName, address.getFullAddress())
            // Opcional: Executa a ação de clique que foi passada no construtor
            onItemClick(address.getFullAddress())
        }

        // Adicionando o clique longo no item para deletar
        holder.itemView.setOnLongClickListener {
            // Remove o endereço do Firebase
//            deleteAddressFromFirebase(userName, address.getFullAddress())
            onItemLongClick(address) // Chama a função passada
            true // Retorna true para indicar que o long click foi consumido
        }

        holder.binding.viewFlipper.displayedChild = 1

    }




    private fun updateItem(position: Int, updatedAddress: Address) {
        val mutableList = currentList.toMutableList()
        mutableList[position] = updatedAddress
        submitList(mutableList)
    }


    // Função para remover um item da lista localmente
    private fun removeItem(position: Int) {
        val mutableList = currentList.toMutableList()
        mutableList.removeAt(position)
        submitList(mutableList) // Atualiza a lista no adaptador
    }

    // Função para salvar o endereço no Firebase
    private fun saveAddressToFirebase(userName: String, fullAddress: String) {
        FirebaseDatabase.getInstance()
            .getReference()
            .child("users")
            .child(userName)
            .child("formulario")
            .child("endereco")
            .setValue(fullAddress)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        context,
                        "Endereço salvo com sucesso!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        "Erro ao salvar endereço no Firebase",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    inner class ViewHolder(val binding: ItemAddressBinding) :

        RecyclerView.ViewHolder(binding.root)

}


//class AddressAdapter(private val onItemClick: (String) -> Unit) :
//    ListAdapter<Address, AddressAdapter.ViewHolder>(DIFF_CALLBACK) {
//
//    companion object {
//        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Address>() {
//            override fun areItemsTheSame(oldItem: Address, newItem: Address): Boolean {
//                return oldItem.cep == newItem.cep
//            }
//
//            override fun areContentsTheSame(oldItem: Address, newItem: Address): Boolean {
//                return oldItem == newItem
//            }
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        return ViewHolder(
//            ItemAddressBinding.inflate(
//                LayoutInflater.from(parent.context),
//                parent,
//                false
//            )
//        )
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val address = getItem(position)
//
//        holder.binding.textAddress.text = address.getFullAddress()
//
//        // Definindo o clique no item
//        holder.itemView.setOnClickListener {
//            onItemClick(address.getFullAddress())  // Passa o endereço completo para a função onItemClick
//        }
//
//        holder.binding.viewFlipper.displayedChild = 1
//    }
//
//    inner class ViewHolder(val binding: ItemAddressBinding) :
//        RecyclerView.ViewHolder(binding.root)
//}
//
////



////class AddressAdapter : ListAdapter<Address, AddressAdapter.ViewHolder>(DIFF_CALLBACK) {
//
//    companion object {
//        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Address>() {
//            override fun areItemsTheSame(
//                oldItem: Address,
//                newItem: Address
//            ): Boolean {
//                return oldItem.cep == newItem.cep
//            }
//
//            override fun areContentsTheSame(
//                oldItem: Address,
//                newItem: Address
//            ): Boolean {
//                return oldItem == newItem
//            }
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        return ViewHolder(
//            ItemAddressBinding.inflate(
//                LayoutInflater.from(parent.context),
//                parent,
//                false
//            )
//        )
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val address = getItem(position)
//
//
//
//        holder.binding.textAddress.text = address.getFullAddress()
//
//        holder.binding.viewFlipper.displayedChild = 1
//    }
//
//    inner class ViewHolder(val binding: ItemAddressBinding) :
//        RecyclerView.ViewHolder(binding.root)
//
//}