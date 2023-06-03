package com.example.mealrecognition.upload.receivers

data class NutrientResponse (
    val foodName: List<String>,
    val hasNutritionalInfo: String,
    val ids: List<Int>,
    val imageId: Int,
    val nutritional_info: NutritionalInfo,
    val nutritional_info_per_item: List<Item>,
    val serving_size: Float
)

data class Item (
    val food_item_position: Int,
    val hasNutritionalInfo: String,
    val id: Int,
    val nutritional_info: NutritionalInfo,
    val serving_size: Float
)

data class NutritionalInfo (
    val calories: Float,
    val dailyIntakeReference: DailyIntakeReference,
    val totalNutrients: TotalNutrients
)

data class TotalNutrients (
    val CA: Type,
    val CHOCDF: Type,
    val CHOLE: Type,
    val ENERC_KCAL: Type,
    val FAMS: Type,
    val FAPU: Type,
    val FASAT: Type,
    val FAT: Type,
    val FATRN: Type,
    val FE: Type,
    val FIBTG: Type,
    val FOLAC: Type,
    val FOLDFE: Type,
    val FOLFD: Type,
    val K: Type,
    val MG: Type,
    val NA: Type,
    val NIA: Type,
    val P: Type,
    val PROCNT: Type,
    val RIBF: Type,
    val SUGAR: Type,
    val THIA: Type,
    val TOCPHA: Type,
    val VITA_RAE: Type,
    val VITB12: Type,
    val VITB6A: Type,
    val VITC: Type,
    val VITD: Type,
    val VITK1: Type,
    val ZN: Type
)

data class Type (
    val label: String,
    val quantity: Float,
    val unit: String
)

data class DailyIntakeReference (
    val CHOCDF: Nutrient,
    val ENERC_KCAL: Nutrient,
    val FASAT: Nutrient,
    val FAT: Nutrient,
    val NA: Nutrient,
    val PROCNT: Nutrient,
    val SUGAR: Nutrient
)

data class Nutrient (
    val label: String,
    val level: String,
    val percent: Float
)