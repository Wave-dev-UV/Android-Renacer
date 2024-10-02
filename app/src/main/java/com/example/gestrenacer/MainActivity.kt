package com.example.gestrenacer

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.gestrenacer.fragment.EditarUsuarioFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Crear un FrameLayout programáticamente
        val frameLayout = FrameLayout(this).apply {
            id = View.generateViewId() // Genera un ID único
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        // Establecer el FrameLayout como el contenido de la actividad
        setContentView(frameLayout)

        // Crear e insertar el fragmento
        val fragment = EditarUsuarioFragment()
        supportFragmentManager.beginTransaction()
            .replace(frameLayout.id, fragment)
            .commit()

        // Manejar los WindowInsets
        ViewCompat.setOnApplyWindowInsetsListener(frameLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
