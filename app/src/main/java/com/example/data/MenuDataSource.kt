package com.example.data

import com.example.model.MenuCategory
import com.example.model.MenuItem
import com.example.model.PortionSize
import com.example.model.SizeOption

object MenuDataSource {
    const val SHOP_NAME = "Slice Smile Pizza Shop"
    const val SHOP_SUBTITLE = "Pizza Workshop"
    const val SHOP_LOCATION = "Chowk Nazir Wala"
    const val MINIMUM_DELIVERY_ORDER = 500
    const val FREE_DELIVERY_RADIUS_KM = 3

    val PHONE_NUMBERS = listOf("0303-7448255", "0303-5574979", "0311-4171141")
    const val SHOP_PHONE = "0303-7448255"
    const val PRIMARY_WHATSAPP = "923037448255"
    const val SHOP_WHATSAPP = "923037448255"
    const val WHATSAPP_URL = "https://wa.me/923037448255"
    const val EASYPAISA_ACCOUNT_NUMBER = "03254946190"
    const val EASYPAISA_ACCOUNT_TITLE = "Slice Smile Pizza Shop"

    val menuItems: List<MenuItem> = listOf(
        // === POPULAR DEALS ===
        MenuItem(
            id = "deal_1",
            name = "Deal No 1",
            category = MenuCategory.DEALS,
            description = "1 Zinger Burger + 1 Regular Fries + 1 Regular Coke",
            basePrice = 430,
            dealIncludes = listOf("1 Zinger Burger", "1 Reg Fries", "1 Reg Coke"),
            isPopular = true,
            tag = "Super Saver"
        ),
        MenuItem(
            id = "deal_2",
            name = "Deal No 2",
            category = MenuCategory.DEALS,
            description = "2 Zinger Burgers + 2 Shawarmas + 1 Litre Regular Coke",
            basePrice = 960,
            dealIncludes = listOf("2 Zinger Burger", "2 Shawarma", "1 L. Reg Coke"),
            isPopular = true,
            tag = "Best Value"
        ),
        MenuItem(
            id = "deal_3",
            name = "Deal No 3",
            category = MenuCategory.DEALS,
            description = "1 Small Pizza + 1 Shawarma + 1 Regular Fries + 1 Regular Coke",
            basePrice = 900,
            dealIncludes = listOf("1 Small Pizza", "1 Shawarma", "1 Reg Fries", "1 Reg Coke"),
            tag = "Combo"
        ),
        MenuItem(
            id = "deal_4",
            name = "Deal No 4",
            category = MenuCategory.DEALS,
            description = "2 Zinger Burgers + 2 Sub Burgers + 1 Regular Fries + 1 Litre Coke",
            basePrice = 1240,
            dealIncludes = listOf("2 Zinger Burger", "2 Sub Burger", "1 Reg Fries", "1 L. Coke")
        ),
        MenuItem(
            id = "deal_5",
            name = "Deal No 5",
            category = MenuCategory.DEALS,
            description = "2 Small Pizzas + 3 Chicken Shawarmas + 1 Litre Coke",
            basePrice = 1680,
            dealIncludes = listOf("2 Small Pizza", "3 Chicken Shawarma", "1 L. Coke"),
            tag = "Party Deal"
        ),
        MenuItem(
            id = "deal_6",
            name = "Deal No 6",
            category = MenuCategory.DEALS,
            description = "1 Medium Pizza + 2 Zinger Burgers + 1 Litre Coke",
            basePrice = 1550,
            dealIncludes = listOf("1 Medium Pizza", "2 Zinger Burger", "1 L. Coke")
        ),
        MenuItem(
            id = "deal_7",
            name = "Deal No 7",
            category = MenuCategory.DEALS,
            description = "4 Shawarmas + 2 Regular Fries + 1 Litre Coke",
            basePrice = 1120,
            dealIncludes = listOf("4 Shawarma", "2 Reg Fries", "1 L. Coke")
        ),
        MenuItem(
            id = "deal_8",
            name = "Deal No 8",
            category = MenuCategory.DEALS,
            description = "1 Large Pizza + 1 Regular Fries + 1 Litre Coke",
            basePrice = 1610,
            dealIncludes = listOf("1 Large Pizza", "1 Reg Fries", "1 L. Coke")
        ),
        MenuItem(
            id = "deal_9",
            name = "Deal No 9",
            category = MenuCategory.DEALS,
            description = "2 Medium Pizzas + 1 Regular Fries + 1 Litre Coke",
            basePrice = 2000,
            dealIncludes = listOf("2 Medium Pizza", "1 Reg Fries", "1 L. Coke")
        ),

        // === FAMILY DEALS ===
        MenuItem(
            id = "family_deal_1",
            name = "Family Deal 1",
            category = MenuCategory.FAMILY_DEALS,
            description = "1 Large Pizza + 2 Zinger Burgers + 1 Large Fries + 1.5 Litre Coke",
            basePrice = 2200,
            dealIncludes = listOf("1 Large Pizza", "2 Zinger", "1 Large Fries", "1.5 L Coke"),
            isPopular = true,
            tag = "Family Feast"
        ),
        MenuItem(
            id = "family_deal_2",
            name = "Family Deal 2",
            category = MenuCategory.FAMILY_DEALS,
            description = "2 Medium Pizzas + 4 Zinger Burgers + 1 Large Fries + 1 Coke Jumbo",
            basePrice = 3150,
            dealIncludes = listOf("2 Medium Pizza", "4 Zinger Burger", "1 Large Fries", "1 Coke Jumbo"),
            tag = "Mega Family"
        ),
        MenuItem(
            id = "family_deal_3",
            name = "Family Deal 3",
            category = MenuCategory.FAMILY_DEALS,
            description = "2 Large Pizzas + 2 Zinger Burgers + 2 Shawarmas + 1 Coke Jumbo",
            basePrice = 3599,
            dealIncludes = listOf("2 Large Pizza", "2 Zinger Burger", "2 Shawarma", "1 Coke Jumbo")
        ),

        // === BIRTHDAY DEALS ===
        MenuItem(
            id = "birthday_deal_1",
            name = "Birthday Deal 1",
            category = MenuCategory.BIRTHDAY_DEALS,
            description = "1 Large Pizza + 4 Zinger Burgers + 4 Shawarmas + 2 Large Fries + 1 Pound Cake + 2.5 Litre Coke",
            basePrice = 4200,
            dealIncludes = listOf("1 Large Pizza", "4 Zinger Burger", "4 Shawarma", "2 Large Fries", "1 Pound Cake", "2.5 L Coke"),
            isPopular = true,
            tag = "Includes Cake!"
        ),

        // === REGULAR PIZZAS ===
        MenuItem(
            id = "pizza_tikka",
            name = "Tikka Pizza",
            category = MenuCategory.PIZZA,
            description = "Traditional spicy chicken tikka chunks, mozzarella cheese, onions & special herb sauce.",
            basePrice = 550,
            sizeOptions = listOf(
                SizeOption(PortionSize.SMALL, 550),
                SizeOption(PortionSize.MEDIUM, 899),
                SizeOption(PortionSize.LARGE, 1399)
            ),
            isPopular = true
        ),
        MenuItem(
            id = "pizza_fajita",
            name = "Fajita Pizza",
            category = MenuCategory.PIZZA,
            description = "Mexican styled fajita chicken, bell peppers, onions, tomatoes & loaded mozzarella.",
            basePrice = 550,
            sizeOptions = listOf(
                SizeOption(PortionSize.SMALL, 550),
                SizeOption(PortionSize.MEDIUM, 899),
                SizeOption(PortionSize.LARGE, 1399)
            )
        ),
        MenuItem(
            id = "pizza_hot_spicy",
            name = "Hot & Spicy Pizza",
            category = MenuCategory.PIZZA,
            description = "Extra fiery spicy marinated chicken, jalapenos, chili flakes & hot sauce drizzle.",
            basePrice = 550,
            sizeOptions = listOf(
                SizeOption(PortionSize.SMALL, 550),
                SizeOption(PortionSize.MEDIUM, 920),
                SizeOption(PortionSize.LARGE, 1440)
            ),
            isSpicy = true
        ),
        MenuItem(
            id = "pizza_supreme",
            name = "Supreme Pizza",
            category = MenuCategory.PIZZA,
            description = "Loaded supreme combination of spiced chicken, black olives, mushrooms, peppers & cheese.",
            basePrice = 570,
            sizeOptions = listOf(
                SizeOption(PortionSize.SMALL, 570),
                SizeOption(PortionSize.MEDIUM, 920),
                SizeOption(PortionSize.LARGE, 1430)
            )
        ),
        MenuItem(
            id = "pizza_veggie",
            name = "Veggie Lover Pizza",
            category = MenuCategory.PIZZA,
            description = "Fresh green capsicum, mushrooms, sweet corn, black olives, onions & mozzarella cheese.",
            basePrice = 550,
            sizeOptions = listOf(
                SizeOption(PortionSize.SMALL, 550),
                SizeOption(PortionSize.MEDIUM, 899),
                SizeOption(PortionSize.LARGE, 1399)
            )
        ),

        // === SPECIAL PIZZAS ===
        MenuItem(
            id = "spec_bonfire",
            name = "Bonfire Pizza",
            category = MenuCategory.SPECIAL_PIZZA,
            description = "Smoky bonfire barbecued chicken pieces, charred veggies, mozzarella cheese & secret pizza sauce.",
            basePrice = 490,
            sizeOptions = listOf(
                SizeOption(PortionSize.SMALL, 490),
                SizeOption(PortionSize.MEDIUM, 999),
                SizeOption(PortionSize.LARGE, 1500)
            ),
            tag = "Chef Special"
        ),
        MenuItem(
            id = "spec_bihari",
            name = "Bihari Pizza",
            category = MenuCategory.SPECIAL_PIZZA,
            description = "Aromatic tender Bihari boti spiced chicken with special desi herbs & melted cheese.",
            basePrice = 599,
            sizeOptions = listOf(
                SizeOption(PortionSize.SMALL, 599),
                SizeOption(PortionSize.MEDIUM, 999),
                SizeOption(PortionSize.LARGE, 1500)
            )
        ),
        MenuItem(
            id = "spec_crown_crust",
            name = "Crown Crust Pizza",
            category = MenuCategory.SPECIAL_PIZZA,
            description = "Signature royal crown-shaped crust pockets stuffed with spiced chicken & cheese bites.",
            basePrice = 599,
            sizeOptions = listOf(
                SizeOption(PortionSize.SMALL, 599),
                SizeOption(PortionSize.MEDIUM, 999),
                SizeOption(PortionSize.LARGE, 1500)
            ),
            isPopular = true,
            tag = "Signature"
        ),
        MenuItem(
            id = "spec_special_stuff",
            name = "Special Stuff Pizza",
            category = MenuCategory.SPECIAL_PIZZA,
            description = "Double stuffed crust overflowing with melted cheese, spicy sausages & tender chicken.",
            basePrice = 599,
            sizeOptions = listOf(
                SizeOption(PortionSize.SMALL, 599),
                SizeOption(PortionSize.MEDIUM, 999),
                SizeOption(PortionSize.LARGE, 1500)
            )
        ),
        MenuItem(
            id = "spec_lazania",
            name = "Lazania Pizza",
            category = MenuCategory.SPECIAL_PIZZA,
            description = "Layered rich Italian lasagne sauce, minced spiced chicken, white sauce & golden baked cheese.",
            basePrice = 599,
            sizeOptions = listOf(
                SizeOption(PortionSize.SMALL, 599),
                SizeOption(PortionSize.MEDIUM, 999),
                SizeOption(PortionSize.LARGE, 1499)
            )
        ),
        MenuItem(
            id = "spec_peri_peri",
            name = "Peri Peri Pizza",
            category = MenuCategory.SPECIAL_PIZZA,
            description = "Tangy & spicy African peri peri grilled chicken, red peppers & signature peri peri swirl.",
            basePrice = 599,
            sizeOptions = listOf(
                SizeOption(PortionSize.SMALL, 599),
                SizeOption(PortionSize.MEDIUM, 999),
                SizeOption(PortionSize.LARGE, 1499)
            ),
            isSpicy = true
        ),
        MenuItem(
            id = "spec_extreme",
            name = "Extreme Pizza",
            category = MenuCategory.SPECIAL_PIZZA,
            description = "Extreme cheese overload, double meat portions, sausages, olives and mushrooms.",
            basePrice = 599,
            sizeOptions = listOf(
                SizeOption(PortionSize.SMALL, 599),
                SizeOption(PortionSize.MEDIUM, 999),
                SizeOption(PortionSize.LARGE, 1499)
            )
        ),
        MenuItem(
            id = "spec_shahi_kabab",
            name = "Shahi Kabab Pizza",
            category = MenuCategory.SPECIAL_PIZZA,
            description = "Royal Mughlai style minced seekh kabab chunks, onions, green chilies & creamy sauce.",
            basePrice = 599,
            sizeOptions = listOf(
                SizeOption(PortionSize.SMALL, 599),
                SizeOption(PortionSize.MEDIUM, 999),
                SizeOption(PortionSize.LARGE, 1499)
            )
        ),
        MenuItem(
            id = "spec_slice_smile",
            name = "Slice Smile Special Pizza",
            category = MenuCategory.SPECIAL_PIZZA,
            description = "Our house masterpiece! Loaded with premium chicken trio, sausages, olives, mushrooms & double cheese.",
            basePrice = 650,
            sizeOptions = listOf(
                SizeOption(PortionSize.SMALL, 650),
                SizeOption(PortionSize.MEDIUM, 999),
                SizeOption(PortionSize.LARGE, 1499),
                SizeOption(PortionSize.EXTRA_LARGE, 2000)
            ),
            isPopular = true,
            tag = "Workshop Special"
        ),
        MenuItem(
            id = "spec_extra_large",
            name = "Extra Large Pizza (Giant 16\")",
            category = MenuCategory.SPECIAL_PIZZA,
            description = "Huge 16-inch giant party pizza loaded with your favorite toppings & cheesy crust.",
            basePrice = 2000,
            sizeOptions = listOf(
                SizeOption(PortionSize.EXTRA_LARGE, 2000)
            ),
            tag = "Party Size"
        ),

        // === BURGERS ===
        MenuItem(
            id = "burger_shami",
            name = "Shami Burger",
            category = MenuCategory.BURGER,
            description = "Crispy traditional spiced shami patty, fresh egg, salad and signature chutney.",
            basePrice = 140
        ),
        MenuItem(
            id = "burger_zinger",
            name = "Zinger Burger",
            category = MenuCategory.BURGER,
            description = "Signature golden crispy fried chicken breast fillet with garlic mayo & crunchy lettuce.",
            basePrice = 280,
            isPopular = true,
            tag = "Top Seller"
        ),
        MenuItem(
            id = "burger_chicken",
            name = "Chicken Burger",
            category = MenuCategory.BURGER,
            description = "Tender grilled chicken patty, fresh onions, cucumber & mayonnaise in toasted bun.",
            basePrice = 250
        ),
        MenuItem(
            id = "burger_chapli",
            name = "Chapli Burger",
            category = MenuCategory.BURGER,
            description = "Peshawari authentic spiced chapli kabab patty with fresh herbs & sliced tomato.",
            basePrice = 250,
            isSpicy = true
        ),
        MenuItem(
            id = "burger_zinger_cheese",
            name = "Zinger Cheese Burger",
            category = MenuCategory.BURGER,
            description = "Extra crispy zinger fillet topped with melted cheddar cheese slice & zesty sauce.",
            basePrice = 320,
            isPopular = true
        ),
        MenuItem(
            id = "burger_sub",
            name = "Sub Burger",
            category = MenuCategory.BURGER,
            description = "Delicious long submarine roll filled with spiced chicken strips, lettuce & sauces.",
            basePrice = 230
        ),

        // === SHAWARMA ===
        MenuItem(
            id = "shw_chicken",
            name = "Chicken Shawarma",
            category = MenuCategory.SHAWARMA,
            description = "Authentic pita bread rolled with juicy roasted chicken, garlic mayo & pickle cabbage.",
            basePrice = 170,
            isPopular = true
        ),
        MenuItem(
            id = "shw_kabab",
            name = "Kabab Shawarma",
            category = MenuCategory.SHAWARMA,
            description = "Spiced grilled chicken seekh kabab wrapped in soft pita bread with garlic sauce.",
            basePrice = 230
        ),
        MenuItem(
            id = "shw_double",
            name = "Double Shawarma",
            category = MenuCategory.SHAWARMA,
            description = "Double portion of spiced chicken meat, double mayo & toasted pita wrap.",
            basePrice = 300,
            tag = "Double Meat"
        ),
        MenuItem(
            id = "shw_arabic",
            name = "Arabic Shawarma",
            category = MenuCategory.SHAWARMA,
            description = "Middle-Eastern style thin bread wrap with marinated chicken, tahini & toum garlic cream.",
            basePrice = 230
        ),
        MenuItem(
            id = "shw_zinger",
            name = "Zinger Shawarma",
            category = MenuCategory.SHAWARMA,
            description = "Crispy golden fried zinger strips wrapped in soft pita with crunchy cabbage & mayo.",
            basePrice = 210,
            isPopular = true
        ),
        MenuItem(
            id = "shw_special",
            name = "Special Shawarma",
            category = MenuCategory.SHAWARMA,
            description = "Slice Smile signature loaded shawarma with chicken, cheese, olives & spicy dressing.",
            basePrice = 300,
            tag = "Special"
        ),

        // === BROAST ===
        MenuItem(
            id = "broast_hot_shot",
            name = "Hot & Shot Broast",
            category = MenuCategory.BROAST,
            description = "Ultra crispy spicy fried broast quarter served with dinner roll, fries & garlic sauce.",
            basePrice = 500,
            isSpicy = true,
            tag = "Crispy Broast"
        ),
        MenuItem(
            id = "broast_leg",
            name = "Leg Broast",
            category = MenuCategory.BROAST,
            description = "Juicy tender fried chicken leg piece seasoned with aromatic secret spices, served with fries.",
            basePrice = 350
        ),
        MenuItem(
            id = "broast_chest",
            name = "Chest Broast",
            category = MenuCategory.BROAST,
            description = "Crisp golden fried chicken breast piece with hot dip & crispy french fries.",
            basePrice = 350
        ),

        // === WINGS & FRIES ===
        MenuItem(
            id = "wings_10pc",
            name = "10 Piece Wings",
            category = MenuCategory.WINGS_FRIES,
            description = "10 pieces of crispy seasoned hot chicken wings with spicy dipping sauce.",
            basePrice = 460,
            isPopular = true
        ),
        MenuItem(
            id = "nuggets_10pc",
            name = "10 Piece Nuggets",
            category = MenuCategory.WINGS_FRIES,
            description = "10 tender bite-sized breaded chicken nuggets with tomato ketchup & garlic mayo.",
            basePrice = 460
        ),
        MenuItem(
            id = "french_fries",
            name = "French Fries",
            category = MenuCategory.WINGS_FRIES,
            description = "Crispy golden salted potato fries with masala seasoning.",
            basePrice = 170,
            sizeOptions = listOf(
                SizeOption(PortionSize.SMALL, 170),
                SizeOption(PortionSize.LARGE, 230)
            )
        ),
        MenuItem(
            id = "loaded_fries",
            name = "Loaded Fries",
            category = MenuCategory.WINGS_FRIES,
            description = "Golden french fries drenched in warm cheddar cheese sauce, chicken chunks & jalapenos.",
            basePrice = 350,
            isPopular = true,
            tag = "Customer Favorite"
        ),

        // === PASTA ===
        MenuItem(
            id = "pasta_alfredo",
            name = "Al Fareedo Pasta",
            category = MenuCategory.PASTA,
            description = "Rich creamy Alfredo white sauce penne pasta with grilled chicken, mushrooms & parmesan.",
            basePrice = 600,
            isPopular = true
        ),
        MenuItem(
            id = "pasta_chicken",
            name = "Chicken Pasta",
            category = MenuCategory.PASTA,
            description = "Savory chicken pasta baked with spiced tomato herb sauce and melted mozzarella topping.",
            basePrice = 550
        ),

        // === WRAPS ===
        MenuItem(
            id = "wrap_twister",
            name = "Twister Roll",
            category = MenuCategory.WRAP,
            description = "Crispy fried zinger fillet rolled in golden paratha with fresh iceberg & pepper mayo.",
            basePrice = 230,
            isPopular = true
        ),
        MenuItem(
            id = "wrap_chicken",
            name = "Chicken Roll",
            category = MenuCategory.WRAP,
            description = "Smoky grilled chicken boti rolled in paratha with onions, mint raita & spicy chutney.",
            basePrice = 230
        ),

        // === CHINESE & PASTA ===
        MenuItem(
            id = "chinese_chowmein",
            name = "Chicken Chowmein",
            category = MenuCategory.CHINESE,
            description = "Wok-tossed egg noodles with shredded chicken breast, cabbage, bell peppers & soya garlic sauce.",
            basePrice = 450,
            isPopular = true,
            tag = "Special Chinese"
        ),
        MenuItem(
            id = "chinese_fried_rice",
            name = "Egg & Chicken Fried Rice",
            category = MenuCategory.CHINESE,
            description = "Aromatic basmati rice stir-fried with scrambled eggs, diced chicken and crunchy spring onions.",
            basePrice = 390
        ),
        MenuItem(
            id = "chinese_manchurian",
            name = "Chicken Manchurian with Rice",
            category = MenuCategory.CHINESE,
            description = "Classic red sweet & sour garlic chicken cubes served piping hot with egg fried rice.",
            basePrice = 580,
            isPopular = true
        ),
        MenuItem(
            id = "chinese_shashlik",
            name = "Chicken Shashlik with Rice",
            category = MenuCategory.CHINESE,
            description = "Tender boneless chicken with capsicum, tomatoes & onions in tangy shashlik gravy + fried rice.",
            basePrice = 590,
            isSpicy = true
        ),
        MenuItem(
            id = "chinese_soup",
            name = "Hot & Sour Soup",
            category = MenuCategory.CHINESE,
            description = "Thick comforting soup with minced chicken, mushrooms, egg drops, black pepper and vinegar.",
            basePrice = 320,
            isSpicy = true
        ),

        // === COLD DRINKS & SODA ===
        MenuItem(
            id = "drink_soft_regular",
            name = "Gourmet Soft Drink (500ml)",
            category = MenuCategory.COLD_DRINKS,
            description = "Chilled 500ml Bottle (Coke, Sprite, Fanta, Dew, Sting Berry).",
            basePrice = 100,
            isPopular = true
        ),
        MenuItem(
            id = "drink_soft_1litre",
            name = "Soft Drink (1 Litre)",
            category = MenuCategory.COLD_DRINKS,
            description = "Chilled 1.0 Litre Family Bottle (Coke / Sprite / Fanta / Dew).",
            basePrice = 180
        ),
        MenuItem(
            id = "drink_soft_1_5litre",
            name = "Soft Drink (1.5 Litre)",
            category = MenuCategory.COLD_DRINKS,
            description = "Chilled 1.5 Litre Big Party Bottle (Coke / Sprite / Dew).",
            basePrice = 240,
            isPopular = true
        ),
        MenuItem(
            id = "drink_soft_jumbo",
            name = "Soft Drink Jumbo (2.25 Litre)",
            category = MenuCategory.COLD_DRINKS,
            description = "Mega 2.25 Litre Jumbo Party Bottle for gatherings.",
            basePrice = 300
        ),
        MenuItem(
            id = "drink_mineral_water",
            name = "Nestle Mineral Water",
            category = MenuCategory.COLD_DRINKS,
            description = "Pure & refreshing chilled mineral water bottle.",
            basePrice = 60,
            sizeOptions = listOf(
                SizeOption(PortionSize.SMALL, 60),
                SizeOption(PortionSize.LARGE, 110)
            )
        ),
        MenuItem(
            id = "drink_mint_margarita",
            name = "Fresh Mint Margarita / Lemonade",
            category = MenuCategory.COLD_DRINKS,
            description = "Freshly blended fresh mint leaves, lemon juice, black salt, crushed ice and 7Up.",
            basePrice = 220,
            isPopular = true,
            tag = "Refreshing"
        ),

        // === JUICES & MILKSHAKES ===
        MenuItem(
            id = "shake_mango",
            name = "Mango Thick Shake",
            category = MenuCategory.JUICES_SHAKES,
            description = "Rich creamy Alphonso mango pulp blended with rich fresh milk and vanilla ice-cream scoop.",
            basePrice = 280,
            isPopular = true,
            tag = "Thick Shake"
        ),
        MenuItem(
            id = "shake_banana_dates",
            name = "Khajoor Banana Milkshake (Special)",
            category = MenuCategory.JUICES_SHAKES,
            description = "Healthy natural energy shake packed with sweet dates (khajoor), fresh bananas, almonds and milk.",
            basePrice = 270,
            tag = "Energy Booster"
        ),
        MenuItem(
            id = "shake_oreo_chocolate",
            name = "Oreo Chocolate Crunch Shake",
            category = MenuCategory.JUICES_SHAKES,
            description = "Crushed Oreo cookies, rich Belgian chocolate syrup, ice cream and chilled milk with whipped topping.",
            basePrice = 320,
            isPopular = true,
            tag = "Kids Favorite"
        ),
        MenuItem(
            id = "shake_strawberry",
            name = "Fresh Strawberry Milkshake",
            category = MenuCategory.JUICES_SHAKES,
            description = "Sweet farm strawberries blended smooth with fresh milk and ice cream.",
            basePrice = 290
        ),
        MenuItem(
            id = "juice_fresh_seasonal",
            name = "Fresh Seasonal Fruit Juice",
            category = MenuCategory.JUICES_SHAKES,
            description = "100% pure fresh squeezed juice (Apple, Orange, Mosambi, or Peach) with no added sugar.",
            basePrice = 260
        ),
        MenuItem(
            id = "shake_doodh_soda",
            name = "Special Rooh Afza Doodh Soda",
            category = MenuCategory.JUICES_SHAKES,
            description = "Classic chilled refreshing blend of fresh milk, Pakola/7Up soda and Rooh Afza rose syrup.",
            basePrice = 190
        )
    )

    val sampleReviews = listOf(
        com.example.model.CustomerFeedback(
            id = 1,
            orderId = 1001,
            customerName = "Ali Raza",
            overallRating = 5,
            foodTasteRating = 5,
            deliverySpeedRating = 5,
            comment = "Crown crust pizza and Zinger burger was super hot and delicious! Fast delivery to Chowk Nazir Wala."
        ),
        com.example.model.CustomerFeedback(
            id = 2,
            orderId = 1002,
            customerName = "Usman Tariq",
            overallRating = 5,
            foodTasteRating = 5,
            deliverySpeedRating = 4,
            comment = "Deal No 2 is unbeatable value. 2 zingers and 2 shawarmas were top quality."
        ),
        com.example.model.CustomerFeedback(
            id = 3,
            orderId = 1003,
            customerName = "Farhan Malik",
            overallRating = 5,
            foodTasteRating = 5,
            deliverySpeedRating = 5,
            comment = "Easypaisa payment was smooth and got my Smile coins loyalty discount on Rs 1500+ order!"
        )
    )
}
