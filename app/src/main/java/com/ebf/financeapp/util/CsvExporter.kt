package com.ebf.financeapp.util



import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.ebf.financeapp.data.model.TransactionWithCategory
import java.io.File
import java.io.FileWriter

object CsvExporter {

    fun export(
        context: Context,
        transactions: List<TransactionWithCategory>,
        fileName: String = "finance_export_${System.currentTimeMillis()}.csv"
    ): Uri {
        val file = File(context.cacheDir, fileName)

        FileWriter(file).use { writer ->
            // Header row
            writer.append("Date,Title,Type,Category,Amount,Note\n")

            // Data rows
            transactions.forEach { twc ->
                val tx   = twc.transaction
                val date = DateFormatter.format(tx.date)
                val type = tx.type.name
                val cat  = twc.category.name.replace(",", " ")
                val title= tx.title.replace(",", " ")
                val note = tx.note.replace(",", " ")
                writer.append("$date,$title,$type,$cat,${tx.amount},$note\n")
            }
        }

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }

    fun shareExport(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type    = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Finance Export")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Export Transactions"))
    }
}