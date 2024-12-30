package com.example.project1732.Activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.project1732.Helper.UserManager;
import com.example.project1732.R;
import com.example.project1732.databinding.ActivityProfileBinding;
import com.example.project1732.via_cep.presenter.MainActivity;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends BaseActivity {

    private static final int REQUEST_CODE_PERMISSIONS = 1001;
    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private ActivityProfileBinding binding;

    TextView username, email, logout, callProfile;
    FirebaseAuth mAuth;
    private CircleImageView circleImageView;

    private static final int REQUEST_PICK_IMAGE = 1002;
    private static final int REQUEST_CAMERA = 1001;

    ProgressDialog pd;
    FirebaseAuth firebaseAuth;
    private UserManager userManager;

    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Inicializa o Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        binding.backBtn.setOnClickListener(v -> finish());

        // Verificar e solicitar permissões
        if (!hasPermissions()) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        callProfile = findViewById(R.id.call_profile);

        mAuth = FirebaseAuth.getInstance();

        email = binding.emailAddress;
        username = binding.userName;
        logout = binding.logout;

        // Set click listener for profile image
        binding.profileimg.setOnClickListener(v ->  checkPermissionsAndShowImagePickerDialog());


        // Load the image from local storage or a placeholder if not available
        loadImage();

        getUserInfo();

        binding.btnHstPedidos.setOnClickListener(v -> {
            getHistoryPedidos();
        });
        binding.btnHistorico.setOnClickListener(v -> {
            getHistoryPedidos();
        });

        binding.changePassword.setOnClickListener(v -> {
            showUpdatePasswordDialog();
        });
        binding.changePassArrow.setOnClickListener(v ->{
            showUpdatePasswordDialog();
        });

        binding.enderecos.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });
        binding.changeEnderecos.setOnClickListener(v ->{
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

        callProfile.setOnClickListener(view -> {
            // Intent para chamada telefônica
//            Intent intent = new Intent(Intent.ACTION_DIAL);
//            intent.setData(Uri.parse("tel:7288918840"));
//            startActivity(intent);
            String numeroLoja = "+5531998666968";
            String url = "https://wa.me/" + numeroLoja;

            try {
                // Intent para abrir o WhatsApp
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            } catch (Exception e) {
                // Exibir mensagem de erro caso o WhatsApp não esteja instalado
                Toast.makeText(this, "WhatsApp não está instalado", Toast.LENGTH_SHORT).show();
            }

        });

        logout.setOnClickListener(view -> {
            deslogarUsuario();
        });
    }

    private void getUserInfo() {
        userManager = UserManager.getInstance(this);
        userManager.refreshUser(); // Garanta que o UserManager tenha o usuário atualizado

        // Obtém o nome e o e-mail do usuário
        String userName = userManager.getUserName();
        String userEmail = userManager.getUserEmail();


        // Atualiza a UI com o nome e e-mail do usuário
        binding.userName.setText(userName);
        binding.emailAddress.setText(userEmail);
    }

    private boolean hasPermissions() {
        for (String permission : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void checkPermissionsAndShowImagePickerDialog() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSIONS);
        } else {
            showImagePickerDialog();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showImagePickerDialog(); // Permissões concedidas, continue a operação
            } else {
                Toast.makeText(this, "Permissões necessárias para salvar a imagem", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_PICK_IMAGE && data != null) {
                Uri imageUri = data.getData();
                saveImageToStorage(imageUri);
                loadImage();  // Carregar a imagem imediatamente após salvar
            } else if (requestCode == REQUEST_CAMERA) {
                saveImageToStorage(imageUri); // Use a URI da câmera
                loadImage();  // Carregar a imagem imediatamente após salvar
            }
        }
    }

    private void saveImageToStorage(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "profile_image.jpg");
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
            outputStream.flush();
            outputStream.close();
            Log.d("ProfileActivity", "Imagem salva localmente: " + file.getAbsolutePath());
            Toast.makeText(this, "Imagem salva: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("ProfileActivity", "Erro ao salvar a imagem: " + e.getMessage());
            Toast.makeText(this, "Erro ao salvar a imagem", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadImage() {
        // Carregar a imagem com o nome "profile_image.jpg"
        File imageFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "profile_image.jpg");
        if (imageFile.exists()) {
            Glide.with(this)
                    .load(imageFile)
                    .into((ImageView) findViewById(R.id.profileimg));  // Certifique-se de que o ID do ImageView está correto
        } else {
            // Se a imagem não estiver disponível, carregue um placeholder
            findViewById(R.id.profileimg).setBackgroundResource(R.drawable.profile); // Substitua com seu placeholder
        }
    }

    private Uri getImageUri(Context context, Bitmap imageBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), imageBitmap, "ProfileImage", null);
        return Uri.parse(path);
    }

    private File createImageFile() {
        String fileName = "profile_image";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir, fileName + ".jpg");
        imageUri = Uri.fromFile(image);
        return image;
    }

    private void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Escolha a origem da imagem")
                .setItems(new CharSequence[]{"Galeria", "Câmera"}, (dialog, which) -> {
                    if (which == 0) {
                        Intent pickImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(pickImageIntent, REQUEST_PICK_IMAGE);
                    } else {
                        openCamera();
                    }
                });
        builder.show();
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                Uri photoUri = FileProvider.getUriForFile(this, "com.example.project1732.fileprovider", photoFile); // Substitua pelo seu pacote
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(cameraIntent, REQUEST_CAMERA);
            }
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_CODE_PERMISSIONS) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Permissões concedidas, continue a operação
//            } else {
//                // Permissões negadas, mostre uma mensagem ao usuário
//                Toast.makeText(this, "Permissões necessárias para salvar a imagem", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == Activity.RESULT_OK) {
//            if (requestCode == REQUEST_PICK_IMAGE && data != null) {
//                Uri imageUri = data.getData();
//                saveImageToStorage(imageUri);
//            } else if (requestCode == REQUEST_CAMERA && data != null) {
//                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
//                Uri imageUri = getImageUri(this, imageBitmap);
//                saveImageToStorage(imageUri);
//            }
//        }
//    }
//
//    private void saveImageToStorage(Uri imageUri) {
//        try {
//            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
//            // Salvar a imagem com o nome "profile_image.jpg"
//            File file = new File(getExternalFilesDir(null), "profile_image.jpg");
//            FileOutputStream outputStream = new FileOutputStream(file);
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
//            outputStream.flush();
//            outputStream.close();
//            Log.d("ProfileActivity", "Imagem salva localmente: " + file.getAbsolutePath());
//            Toast.makeText(this, "Imagem salva: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
//        } catch (IOException e) {
//            Log.e("ProfileActivity", "Erro ao salvar a imagem: " + e.getMessage());
//            Toast.makeText(this, "Erro ao salvar a imagem", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private void loadImage() {
//        // Carregar a imagem com o nome "profile_image.jpg"
//        File imageFile = new File(getExternalFilesDir(null), "profile_image.jpg");
//        if (imageFile.exists()) {
//            Glide.with(this)
//                    .load(imageFile)
//                    .into(binding.profileimg);
//        } else {
//            // Se a imagem não estiver disponível, carregue um placeholder
//            binding.profileimg.setImageResource(R.drawable.profile); // Substitua com seu placeholder
//        }
//    }
//
//    private Uri getImageUri(Context context, Bitmap imageBitmap) {
//        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
//        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), imageBitmap, "ProfileImage", null);
//        return Uri.parse(path);
//    }
//
//
//    private void showImagePickerDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Choose Image Source")
//                .setItems(new CharSequence[]{"Gallery", "Camera"}, (dialog, which) -> {
//                    if (which == 0) {
//                        Intent pickImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                        startActivityForResult(pickImageIntent, REQUEST_PICK_IMAGE);
//                    } else {
//                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                        startActivityForResult(cameraIntent, REQUEST_CAMERA);
//                    }
//                });
//        builder.show();
//    }


    private void showUpdatePasswordDialog() {
        DialogTrocaSenha.showTrocaSenhaDialog(
                this,
                "TROCAR SENHA",
                (senhaAntiga, novaSenha) -> updatePassword(senhaAntiga, novaSenha),
                () -> Toast.makeText(this, "Operação cancelada", Toast.LENGTH_SHORT).show()
        );
    }

    private void updatePassword(String oldPassword, String newPassword) {
        if (firebaseAuth == null) {
            Toast.makeText(this, "Firebase Auth não inicializado", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);
        user.reauthenticate(authCredential)
                .addOnSuccessListener(unused -> user.updatePassword(newPassword)
                        .addOnSuccessListener(unused1 ->
                                Toast.makeText(ProfileActivity.this, "Senha Atualizada...", Toast.LENGTH_SHORT).show()
                        )
                        .addOnFailureListener(e ->
                                Toast.makeText(ProfileActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show()
                        )
                )
                .addOnFailureListener(e ->
                        Toast.makeText(ProfileActivity.this, "A senha atual está incorreta. Verifique e tente novamente.", Toast.LENGTH_SHORT).show()
                );
    }

    private void getHistoryPedidos() {
        Intent intent = new Intent(this, OrderHistoryActivity.class);
        startActivity(intent);

    }

    private void deslogarUsuario() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Confirmação")
                .setMessage("Você realmente deseja sair?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    finishAffinity();
                    //UserManager.getInstance(MainActivity.this).refreshUser(); // Atualiza o UserManager
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                    //finish();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

}