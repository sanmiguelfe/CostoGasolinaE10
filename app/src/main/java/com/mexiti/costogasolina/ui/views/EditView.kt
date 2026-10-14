package com.mexiti.costogasolina.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mexiti.costogasolina.R
import com.mexiti.costogasolina.domain.calcularMonto
import com.mexiti.costogasolina.model.GasolineTransaction
import com.mexiti.costogasolina.ui.components.AddOptionalData
import com.mexiti.costogasolina.ui.components.EditNumberField
import com.mexiti.costogasolina.ui.components.MainTitle
import com.mexiti.costogasolina.viewmodel.GasolineViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditView(
    transactionId: Int,
    navController: NavController,
    viewModel: GasolineViewModel,
    modifier: Modifier = Modifier
) {
    // Aquí se almacenará el registro que queremos modificar.
    var transaction by remember(transactionId) {
        mutableStateOf<GasolineTransaction?>(null)
    }

    // Indica si todavía estamos consultando la base de datos.
    var isLoading by remember(transactionId) {
        mutableStateOf(true)
    }

    // Consulta el registro al abrir la pantalla.
    LaunchedEffect(transactionId) {
        transaction = viewModel.getTransactionById(transactionId)
        isLoading = false
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    MainTitle(title = "Editar Registro")
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { innerPadding ->

        if (isLoading) {

            // Muestra un indicador mientras se consulta el registro.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

        } else if (transaction != null) {

            // Muestra el formulario con los datos del registro.
            EditContent(
                transaction = transaction!!,
                onUpdateTransaction = { updatedTransaction ->
                    viewModel.updateTransaction(updatedTransaction)

                    scope.launch {
                        snackbarHostState.showSnackbar(
                            "Registro actualizado correctamente"
                        )
                        delay(800)
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.padding(innerPadding)
            )

        } else {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Registro no encontrado")
            }
        }
    }
}

@Composable
fun EditContent(
    transaction: GasolineTransaction,
    onUpdateTransaction: (GasolineTransaction) -> Unit,
    modifier: Modifier = Modifier
) {
    // Los campos comienzan con los valores guardados anteriormente.
    var precioLitroEntrada by remember {
        mutableStateOf(transaction.pricePerLiter.toString())
    }

    var cantLitrosEntrada by remember {
        mutableStateOf(transaction.liters.toString())
    }

    var propinaEntrada by remember {
        mutableStateOf(
            if (transaction.tip > 0) transaction.tip.toString() else ""
        )
    }

    var darPropina by remember {
        mutableStateOf(transaction.tip > 0)
    }

    var guardarKilometros by remember {
        mutableStateOf(transaction.kilometers > 0)
    }

    var kilometrosEntrada by remember {
        mutableStateOf(
            if (transaction.kilometers > 0) {
                transaction.kilometers.toString()
            } else {
                ""
            }
        )
    }

    // Convierte el texto de los campos a números.
    val precioLitro = precioLitroEntrada.toDoubleOrNull() ?: 0.0
    val cantLitros = cantLitrosEntrada.toDoubleOrNull() ?: 0.0

    val propina = if (darPropina) {
        propinaEntrada.toDoubleOrNull() ?: 0.0
    } else {
        0.0
    }

    val kilometraje = if (guardarKilometros) {
        kilometrosEntrada.toDoubleOrNull() ?: 0.0
    } else {
        0.0
    }

    // Reutiliza la función de cálculo del proyecto.
    val total = calcularMonto(
        precio = precioLitro,
        cantLitros = cantLitros,
        darPropina = darPropina,
        propina = propina
    )

    // El precio y los litros deben ser mayores que cero.
    val isFormValid = precioLitro > 0.0 && cantLitros > 0.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // Campo: precio por litro.
                EditNumberField(
                    label = R.string.ingresa_gasolina,
                    leadingIcon = R.drawable.money_gas,
                    keyboardsOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    value = precioLitroEntrada,
                    onValueChanged = { precioLitroEntrada = it }
                )

                // Campo: cantidad de litros.
                EditNumberField(
                    label = R.string.litros,
                    leadingIcon = R.drawable.gasolina,
                    keyboardsOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    value = cantLitrosEntrada,
                    onValueChanged = { cantLitrosEntrada = it }
                )

                // Interruptor para incluir propina.
                AddOptionalData(
                    flagData = darPropina,
                    onflagCheckedChange = { darPropina = it }
                )

                AnimatedVisibility(
                    visible = darPropina,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    EditNumberField(
                        label = R.string.propina,
                        leadingIcon = R.drawable.outline_18_up_rating_24,
                        keyboardsOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        value = propinaEntrada,
                        onValueChanged = { propinaEntrada = it }
                    )
                }

                // Interruptor para incluir kilometraje.
                AddOptionalData(
                    flagData = guardarKilometros,
                    onflagCheckedChange = { guardarKilometros = it },
                    question = stringResource(R.string.guardar_kilometraje)
                )

                AnimatedVisibility(
                    visible = guardarKilometros,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    EditNumberField(
                        label = R.string.kilometros,
                        leadingIcon = R.drawable.car__kilometer,
                        keyboardsOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        value = kilometrosEntrada,
                        onValueChanged = { kilometrosEntrada = it }
                    )
                }
            }
        }

        // Tarjeta que muestra el total calculado.
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.monto_total, total),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Button(
            onClick = {
                // Conserva el ID y la fecha del registro original.
                // Solo cambia los datos editados en el formulario.
                val updatedTransaction = transaction.copy(
                    pricePerLiter = precioLitro,
                    liters = cantLitros,
                    tip = propina,
                    kilometers = kilometraje
                )

                onUpdateTransaction(updatedTransaction)
            },
            enabled = isFormValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Guardar Cambios")
        }
    }
}