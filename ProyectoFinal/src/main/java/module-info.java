module co.edu.uniquindio.proyectofinal {
    requires javafx.controls;
    requires javafx.fxml;

    opens co.edu.uniquindio.proyectofinal to javafx.fxml;
    opens co.edu.uniquindio.proyectofinal.vistas to javafx.fxml;
    opens co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos to javafx.base;

    exports co.edu.uniquindio.proyectofinal;
    exports co.edu.uniquindio.proyectofinal.vistas;
    exports co.edu.uniquindio.proyectofinal.Estructuras.Arbol;
    exports co.edu.uniquindio.proyectofinal.Estructuras.Colas;
    exports co.edu.uniquindio.proyectofinal.Estructuras.Controllers;
    exports co.edu.uniquindio.proyectofinal.Estructuras.Grafos;
    exports co.edu.uniquindio.proyectofinal.Estructuras.Hash;
    exports co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos;
    exports co.edu.uniquindio.proyectofinal.Estructuras.Pilas;
    exports co.edu.uniquindio.proyectofinal.Estructuras.Servicios;
    exports co.edu.uniquindio.proyectofinal.Estructuras.SimpleList;
}
