package com.example.tobetv.ui.vod

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tobetv.data.model.VodItem
import com.example.tobetv.ui.components.TvCard
import com.example.tobetv.ui.theme.TobeAccentPrimary
import com.example.tobetv.ui.theme.TobeBgDeep
import com.example.tobetv.ui.theme.TobeGlassStroke
import com.example.tobetv.ui.theme.TobeSurfaceBright
import com.example.tobetv.ui.theme.TobeTextMuted
import com.example.tobetv.ui.theme.TobeTextPrimary
import com.example.tobetv.ui.theme.TobeTextSecondary

@Composable
fun VodCatalogScreen(
    items: List<VodItem>,
    onPlayVod: (VodItem) -> Unit,
    onBack: () -> Unit
) {
    val categories = remember(items) {
        listOf("Semua") + items.map { it.category }.distinct()
    }
    var selectedCategory by remember { mutableStateOf("Semua") }

    val filteredItems = remember(items, selectedCategory) {
        if (selectedCategory == "Semua") items else items.filter { it.category == selectedCategory }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TobeBgDeep)
            .padding(horizontal = 48.dp, vertical = 28.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TvCard(
                    onClick = onBack,
                    shape = RoundedCornerShape(10.dp)
                ) { isFocused ->
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = if (isFocused) TobeBgDeep else TobeAccentPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Kembali",
                            color = if (isFocused) TobeBgDeep else TobeTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                Text(
                    text = "Bioskop & Film Indonesia (${filteredItems.size} Judul)",
                    color = TobeTextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Genre / Category Bar
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    val isSelected = category == selectedCategory
                    TvCard(
                        onClick = { selectedCategory = category },
                        shape = RoundedCornerShape(10.dp),
                        focusedBorderColor = TobeAccentPrimary,
                        unfocusedBorderColor = if (isSelected) TobeAccentPrimary else TobeGlassStroke
                    ) { isFocused ->
                        Box(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = category,
                                color = if (isFocused || isSelected) TobeAccentPrimary else TobeTextSecondary,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Grid of VOD Cards (4 Columns for Cinema Look)
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredItems) { vod ->
                    TvCard(
                        onClick = { onPlayVod(vod) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) { isFocused ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(210.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(TobeSurfaceBright)
                            ) {
                                if (!vod.posterUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = vod.posterUrl,
                                        contentDescription = vod.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                if (isFocused) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .align(Alignment.Center)
                                            .background(TobeAccentPrimary, RoundedCornerShape(27.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = TobeBgDeep,
                                            modifier = Modifier.size(34.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = vod.title,
                                color = if (isFocused) TobeAccentPrimary else TobeTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = vod.category,
                                    color = TobeTextMuted,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "${vod.year ?: ""} • ${vod.duration ?: ""}",
                                    color = TobeTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
