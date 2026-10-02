package com.kurupdevs.woodnest.data.db

object SeedData {

    val products: List<Product> = listOf(
        Product(
            "oslo-sofa", "Oslo 3-Seater Sofa", "Sofas", 34999, 49999,
            "Deep-cushioned 3-seater in premium beige fabric with solid wood legs. Built for long evenings.",
            "Fabric + Sheesham wood", "sofa", 4.6f, 212,
            210, 85, 90, 220, 95, 100, 14, null
        ),
        Product(
            "nordic-coffee-table", "Nordic Coffee Table", "Tables", 8999, 12999,
            "Minimal solid-wood coffee table with a smooth matte finish and lower shelf.",
            "Sheesham wood", "coffee_table", 4.5f, 164,
            110, 40, 60, 120, 50, 70, 22, null
        ),
        Product(
            "haven-dining-table", "Haven 6-Seater Dining Table", "Tables", 27999, 39999,
            "Solid wood 6-seater dining table with a rich walnut finish. Seats the whole family.",
            "Sheesham wood", "dining_table", 4.7f, 189,
            180, 75, 90, 190, 85, 100, 9, "north"
        ),
        Product(
            "drift-nightstand", "Drift Nightstand", "Bedroom", 5499, 7999,
            "Compact bedside table with a soft-close drawer and open shelf.",
            "Engineered wood + veneer", "nightstand", 4.4f, 143,
            45, 55, 40, 55, 65, 50, 30, "north"
        ),
        Product(
            "marco-counter-stool", "Marco Counter Stool", "Chairs", 6999, 9499,
            "Ergonomic counter stool with moulded seat and sturdy metal legs.",
            "Plastic shell + steel", "counter_stool", 4.3f, 98,
            45, 75, 45, 55, 85, 55, 26, "south"
        ),
        Product(
            "halo-arch-mirror", "Halo Arch Mirror", "Decor", 7499, 10999,
            "Full-length arch mirror with a slim matte frame. Makes small rooms feel bigger.",
            "Glass + aluminium", "arch_mirror", 4.6f, 176,
            70, 170, 5, 80, 180, 15, 18, "south"
        ),
        Product(
            "ember-accent-chair", "Ember Accent Chair", "Chairs", 14999, 21999,
            "Sculptural accent chair in burnt-orange weave with solid wood frame.",
            "Fabric + wood", "accent_chair", 4.5f, 121,
            70, 80, 75, 80, 90, 85, 12, "south"
        ),
        Product(
            "arc-floor-lamp", "Arc Floor Lamp", "Lighting", 4999, 8499,
            "Minimal arc floor lamp with warm dimmable light. A corner's best friend.",
            "Metal + fabric shade", "floor_lamp", 4.4f, 187,
            40, 160, 40, 50, 170, 50, 25, "south"
        ),
        Product(
            "sheesham-bed", "Sheesham Bed Frame", "Bedroom", 29999, 42999,
            "Queen-size bed in solid sheesham with a tall panel headboard. No creaks, ever.",
            "Solid sheesham", "bed_frame", 4.8f, 243,
            160, 120, 210, 170, 130, 220, 7, "north"
        ),
        Product(
            "atlas-bookshelf", "Atlas Bookshelf", "Storage", 11999, 16999,
            "5-tier open bookshelf in solid wood. Holds ~80 books without wobbling.",
            "Sheesham wood", "bookshelf", 4.5f, 132,
            90, 180, 35, 100, 190, 45, 15, "north"
        ),
        Product(
            "focus-desk", "Focus Work Desk", "Tables", 9999, 14499,
            "Compact work desk with cable slot and a sturdy matte top.",
            "Engineered wood", "desk", 4.3f, 109,
            120, 75, 60, 130, 85, 70, 20, "south"
        )
    )

    private val DAY_MS = 86400000L
    private val now = System.currentTimeMillis()

    val priceSeeds: List<PriceHistory> = listOf(
        PriceHistory(0, "arc-floor-lamp", 5499, now - 7 * DAY_MS),
        PriceHistory(0, "arc-floor-lamp", 4999, now)
    )

    private val names = listOf(
        "Aarav M.", "Diya S.", "Kabir R.", "Ananya I.", "Vivaan P.", "Ishaan K.",
        "Meera J.", "Arjun N.", "Sara T.", "Rohan B.", "Priya L.", "Aditya G."
    )
    private val texts = listOf(
        "Solid build, finish looks premium. Delivery was smooth and packing was excellent.",
        "Value for money. Matches my room perfectly and feels very sturdy.",
        "Great quality for the price. Assembly was quick, no wobbling at all.",
        "Looks even better in person. Highly recommended if you're setting up a new home.",
        "Sturdy and comfortable. Delivery took a day extra but worth the wait.",
        "Clean design, exactly like the photos. My whole family loves it.",
        "Premium feel, solid wood grain. Would definitely buy from here again.",
        "Good purchase. Packaging kept everything scratch-free.",
        "Elegant and durable. The color matches my decor beautifully.",
        "Comfortable and well-made. Customer support helped with slot timing.",
        "Excellent craftsmanship. Finishing touches are really nice.",
        "Worth every rupee. Feels like a much more expensive piece."
    )

    val reviewSeeds: List<Review> = run {
        val pids = listOf(
            "oslo-sofa", "sheesham-bed", "haven-dining-table",
            "arc-floor-lamp", "ember-accent-chair", "atlas-bookshelf"
        )
        val out = mutableListOf<Review>()
        pids.forEachIndexed { pi, pid ->
            for (j in 0..1) {
                val k = pi * 2 + j
                out.add(
                    Review(
                        0,
                        pid,
                        names[k],
                        if (k % 3 == 0) 4 else 5,
                        texts[k],
                        null,
                        true,
                        now - (k + 1) * 2 * DAY_MS
                    )
                )
            }
        }
        out
    }
}
