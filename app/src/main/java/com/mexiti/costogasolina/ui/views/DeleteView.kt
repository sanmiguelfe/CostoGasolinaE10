package com.mexiti.costogasolina.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mexiti.costogasolina.R
import com.mexiti.costogasolina.domain.calcularMonto
import com.mexiti.costogasolina.model.GasolineTransaction
import com.mexiti.costogasolina.ui.components.MainTitle
import com.mexiti.costogasolina.viewmodel.GasolineViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteView(
    transactionId: Int,
    navController: NavController,
    viewModel: GasolineViewModel,
    modifier: Modifier = Modifier
) {
    // Almacena el registro seleccionado.
    var transaction by remember(transactionId) {
        mutableStateOf<GasolineTransaction?>(null)
    }

    var isLoading by remember(transactionId) {
        mutableStateOf(true)
    }

    // Busca el registro para mostrar sus datos antes de eliminarlo.
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
                    MainTitle(title = "Eliminar Registro")
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
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    titleContentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { innerPadding ->

        if (isLoading) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

        } else if (transaction != null) {

            DeleteContent(
                transaction = transaction!!,
                onConfirmDelete = { selectedTransaction ->
                    viewModel.deleteTransaction(selectedTransaction)

                    scope.launch {
                        snackbarHostState.showSnackbar(
                            "Registro eliminado"
                        )
                        delay(800)
                        navController.popBackStack()
                    }
                },
                onCancel = {
                    navController.popBackStack()
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
                Text(
                    text = "El registro no existe o ya fue eliminado"
                )
            }
        }
    }
}

@Composable
fun DeleteContent(
    transaction: GasolineTransaction,
    onConfirmDelete: (GasolineTransaction) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Calcula el total del registro seleccionado.
    val total = calcularMonto(
        precio = transaction.pricePerLiter,
        cantLitros = transaction.liters,
        darPropina = transaction.tip > 0,
        propina = transaction.tip
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Advertencia",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )

        Text(
            text = "¿Deseas eliminar este registro?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )

        // Muestra los datos para comprobar qué registro se eliminará.
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Text(
                    text = "Fecha: ${transaction.date}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Precio por litro: $${transaction.pricePerLiter}"
                )

                Text(
                    text = "Litros: ${transaction.liters} L"
                )

                if (transaction.tip > 0) {
                    Text(
                        text = "Propina: $${transaction.tip}"
                    )
                }

                if (transaction.kilometers > 0) {
                    Text(
                        text = "Kilometraje: ${transaction.kilometers} km"
                    )
                }

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = stringResource(R.string.monto_total, total),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Ofrece cancelar o confirmar la eliminación.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Cancelar")
            }

            Button(
                onClick = {
                    onConfirmDelete(transaction)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Eliminar")
            }
        }
    }
}