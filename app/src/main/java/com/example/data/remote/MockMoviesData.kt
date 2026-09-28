package com.example.data.remote

import com.example.data.model.CastMember
import com.example.data.model.Movie

object MockMoviesData {

    val heroMovie = Movie(
        id = 693134,
        title = "Dune: Part Two",
        overview = "Follow the mythic journey of Paul Atreides as he unites with Chani and the Fremen while on a path of revenge against the conspirators who destroyed his family. Facing a choice between the love of his life and the fate of the known universe, he endeavors to prevent a terrible future only he can foresee.",
        posterPath = "https://image.tmdb.org/t/p/w780/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg",
        backdropPath = "https://image.tmdb.org/t/p/w1280/xOMo8BRK7PfcJv9JCnx7s5hj0PX.jpg",
        releaseDate = "2024-03-01",
        voteAverage = 8.2,
        voteCount = 4850,
        runtime = "2h 46m",
        genres = listOf("Sci-Fi", "Adventure", "Action"),
        category = "trending",
        isTvShow = false,
        trailerYoutubeId = "Way9Dexny3w",
        server1StreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
        server2StreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
        cast = listOf(
            CastMember(1, "Timothée Chalamet", "Paul Atreides", "https://image.tmdb.org/t/p/w185/BE2sdjpgsa2rNTFa66f7upkaOP.jpg"),
            CastMember(2, "Zendaya", "Chani", "https://image.tmdb.org/t/p/w185/r3A7evOEOVKaBmNG4Bp090T6Qii.jpg"),
            CastMember(3, "Rebecca Ferguson", "Lady Jessica", "https://image.tmdb.org/t/p/w185/6NRn5L95mpyWW3vGfF2kW00jBzg.jpg"),
            CastMember(4, "Austin Butler", "Feyd-Rautha", "https://image.tmdb.org/t/p/w185/qj1M39r2Y2uK28Qd8zZ2kQjC1u9.jpg"),
            CastMember(5, "Florence Pugh", "Princess Irulan", "https://image.tmdb.org/t/p/w185/750r67V1pS3bXm8Pj7i4cM4xNq.jpg")
        ),
        arabicTitle = "كثيب: الجزء الثاني",
        arabicOverview = "يتابع الفيلم الرحلة الأسطورية لبول آتريديز وهو يتحد مع تشاني وشعب الفريمن في سبيل الانتقام من المتآمرين الذين دمروا عائلته، في صراع مصيري ملحمي."
    )

    val trendingMovies = listOf(
        heroMovie,
        Movie(
            id = 533535,
            title = "Deadpool & Wolverine",
            overview = "A listless Wade Wilson toils away in civilian life with his days as the morally flexible mercenary, Deadpool, behind him. But when his homeworld faces an existential threat, Wade must reluctantly suit-up again with an even more reluctant Wolverine.",
            posterPath = "https://image.tmdb.org/t/p/w780/8cdWjvZQUExUUTzyp4t6EDMubfO.jpg",
            backdropPath = "https://image.tmdb.org/t/p/w1280/yDHYTfA3R0jFYba16jBB12vYzy.jpg",
            releaseDate = "2024-07-26",
            voteAverage = 7.7,
            voteCount = 3890,
            runtime = "2h 08m",
            genres = listOf("Action", "Comedy", "Sci-Fi"),
            category = "trending",
            trailerYoutubeId = "73_1biulkYk",
            server1StreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            server2StreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
            cast = listOf(
                CastMember(11, "Ryan Reynolds", "Wade Wilson / Deadpool", "https://image.tmdb.org/t/p/w185/h1co81Qa92040dA04.jpg"),
                CastMember(12, "Hugh Jackman", "Logan / Wolverine", "https://image.tmdb.org/t/p/w185/4XujBEq4qrm9n0NwhpQjK.jpg"),
                CastMember(13, "Emma Corrin", "Cassandra Nova", "https://image.tmdb.org/t/p/w185/9Y9mY0W41n2gQkP9o.jpg")
            ),
            arabicTitle = "ديدبول وولفرين",
            arabicOverview = "يعود وايد ويلسون (ديدبول) لارتداء بدلة البطل بعد تهديد وجودي يطال عالمه، مجبراً إياه على الاتحاد مع ولفيرين في مواجهة خطيرة مليئة بالأكشن والضحك."
        ),
        Movie(
            id = 872585,
            title = "Oppenheimer",
            overview = "The story of J. Robert Oppenheimer's role in the development of the atomic bomb during World War II, exploring the profound moral consequences and political fallout.",
            posterPath = "https://image.tmdb.org/t/p/w780/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg",
            backdropPath = "https://image.tmdb.org/t/p/w1280/nb3xI8XI3w4pMVZ38VijbsyBqP4.jpg",
            releaseDate = "2023-07-21",
            voteAverage = 8.1,
            voteCount = 8900,
            runtime = "3h 00m",
            genres = listOf("Drama", "History", "Biography"),
            category = "trending",
            trailerYoutubeId = "uYPbbksJxIg",
            server1StreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            server2StreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            cast = listOf(
                CastMember(21, "Cillian Murphy", "J. Robert Oppenheimer", "https://image.tmdb.org/t/p/w185/360Er20.jpg"),
                CastMember(22, "Emily Blunt", "Katherine Oppenheimer", "https://image.tmdb.org/t/p/w185/nPJMg9.jpg"),
                CastMember(23, "Robert Downey Jr.", "Lewis Strauss", "https://image.tmdb.org/t/p/w185/1YjdSym1.jpg")
            ),
            arabicTitle = "أوبنهايمر",
            arabicOverview = "قصة ملحمية تاريخية تستعرض دور الفيزيائي روبرت أوبنهايمر في تطوير السلاح النووي خلال مشروع مانهاتن والتحديات الأخلاقية التي غيرت مسار التاريخ."
        ),
        Movie(
            id = 558449,
            title = "Gladiator II",
            overview = "Years after witnessing the death of the revered hero Maximus at the hands of his uncle, Lucius must enter the Colosseum after his home is conquered by the tyrannical Emperors who now lead Rome.",
            posterPath = "https://image.tmdb.org/t/p/w780/2cxhvwyEwRlysAmRH4iodkvo0z5.jpg",
            backdropPath = "https://image.tmdb.org/t/p/w1280/euYIwmwkmz95mnExrPyPtAhPkmn.jpg",
            releaseDate = "2024-11-22",
            voteAverage = 7.1,
            voteCount = 2100,
            runtime = "2h 28m",
            genres = listOf("Action", "Adventure", "Drama"),
            category = "trending",
            trailerYoutubeId = "4rgYUipGJNo",
            server1StreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackSeeTheWorld.mp4",
            server2StreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            cast = listOf(
                CastMember(31, "Paul Mescal", "Lucius Verus", "https://image.tmdb.org/t/p/w185/pM3bY89.jpg"),
                CastMember(32, "Pedro Pascal", "General Acacius", "https://image.tmdb.org/t/p/w185/904o9.jpg"),
                CastMember(33, "Denzel Washington", "Macrinus", "https://image.tmdb.org/t/p/w185/jj888.jpg")
            )
        ),
        Movie(
            id = 569094,
            title = "Spider-Man: Across the Spider-Verse",
            overview = "After reuniting with Gwen Stacy, Brooklyn’s full-time, friendly neighborhood Spider-Man is catapulted across the Multiverse, where he encounters the Spider Society, a team of Spider-People charged with protecting the Multiverse’s very existence.",
            posterPath = "https://image.tmdb.org/t/p/w780/8Vt6mWEReuy4Of61Lnj5Xj704m8.jpg",
            backdropPath = "https://image.tmdb.org/t/p/w1280/4HodYYKEIsGOdinkGi2Ucz6X9i0.jpg",
            releaseDate = "2023-06-02",
            voteAverage = 8.4,
            voteCount = 6500,
            runtime = "2h 20m",
            genres = listOf("Animation", "Action", "Adventure"),
            category = "trending",
            trailerYoutubeId = "cqGjhVJWtEg",
            server1StreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            server2StreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
            cast = listOf(
                CastMember(41, "Shameik Moore", "Miles Morales", "https://image.tmdb.org/t/p/w185/213m.jpg"),
                CastMember(42, "Hailee Steinfeld", "Gwen Stacy", "https://image.tmdb.org/t/p/w185/380.jpg")
            )
        )
    )

    val topRatedMovies = listOf(
        Movie(
            id = 278,
            title = "The Shawshank Redemption",
            overview = "Imprisoned in the 1940s for the double murder of his wife and her lover, upstanding banker Andy Dufresne begins a new life at the Shawshank prison, where he puts his accounting skills to work for an amoral warden.",
            posterPath = "https://image.tmdb.org/t/p/w780/9cqNxx0GxF0bflZmeSMuL5tnGzr.jpg",
            backdropPath = "https://image.tmdb.org/t/p/w1280/kXfqcdQKsToO0OUXHcrrNCHDBzO.jpg",
            releaseDate = "1994-09-23",
            voteAverage = 8.7,
            voteCount = 26500,
            runtime = "2h 22m",
            genres = listOf("Drama", "Crime"),
            category = "top_rated",
            trailerYoutubeId = "PLl99DlL6b4",
            server1StreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            server2StreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            cast = listOf(
                CastMember(51, "Tim Robbins", "Andy Dufresne", "https://image.tmdb.org/t/p/w185/4t2s.jpg"),
                CastMember(52, "Morgan Freeman", "Ellis Boyd 'Red' Redding", "https://image.tmdb.org/t/p/w185/51f.jpg")
            )
        ),
        Movie(
            id = 238,
            title = "The Godfather",
            overview = "Spanning the years 1945 to 1955, a chronicle of the fictional Italian-American Corleone crime family. When organized crime family patriarch, Vito Corleone barely survives an attempt on his life, his youngest son, Michael steps in.",
            posterPath = "https://image.tmdb.org/t/p/w780/3bhkrj58Vtu7enYsRolD1fZdja1.jpg",
            backdropPath = "https://image.tmdb.org/t/p/w1280/tmU7GeKVybMWF9YOCagmsgn9XYd.jpg",
            releaseDate = "1972-03-14",
            voteAverage = 8.7,
            voteCount = 19800,
            runtime = "2h 55m",
            genres = listOf("Drama", "Crime"),
            category = "top_rated",
            trailerYoutubeId = "UaVTIH8mujA",
            server1StreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
            server2StreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            cast = listOf(
                CastMember(61, "Marlon Brando", "Don Vito Corleone", "https://image.tmdb.org/t/p/w185/77.jpg"),
                CastMember(62, "Al Pacino", "Michael Corleone", "https://image.tmdb.org/t/p/w185/88.jpg")
            )
        ),
        Movie(
            id = 157336,
            title = "Interstellar",
            overview = "The adventures of a group of explorers who make use of a newly discovered wormhole to surpass the limitations on human space travel and conquer the vast distances involved in an interstellar voyage.",
            posterPath = "https://image.tmdb.org/t/p/w780/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
            backdropPath = "https://image.tmdb.org/t/p/w1280/xJHokMbljvjADYdit5fK5VQsXEG.jpg",
            releaseDate = "2014-11-05",
            voteAverage = 8.4,
            voteCount = 34500,
            runtime = "2h 49m",
            genres = listOf("Adventure", "Drama", "Sci-Fi"),
            category = "top_rated",
            trailerYoutubeId = "zSWdZVtXT7E",
            server1StreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            server2StreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            cast = listOf(
                CastMember(71, "Matthew McConaughey", "Joseph Cooper", "https://image.tmdb.org/t/p/w185/711.jpg"),
                CastMember(72, "Anne Hathaway", "Dr. Amelia Brand", "https://image.tmdb.org/t/p/w185/712.jpg"),
                CastMember(73, "Jessica Chastain", "Murphy Cooper", "https://image.tmdb.org/t/p/w185/713.jpg")
            )
        ),
        Movie(
            id = 155,
            title = "The Dark Knight",
            overview = "Batman raises the stakes in his war on crime. With the help of Lt. Jim Gordon and District Attorney Harvey Dent, Batman sets out to dismantle the remaining criminal organizations that plague the streets.",
            posterPath = "https://image.tmdb.org/t/p/w780/qJ2tW6WMUDux911r6m7haRef0WH.jpg",
            backdropPath = "https://image.tmdb.org/t/p/w1280/nMKdUUepR0i5zn0y1T4CsSB5chy.jpg",
            releaseDate = "2008-07-16",
            voteAverage = 8.5,
            voteCount = 32100,
            runtime = "2h 32m",
            genres = listOf("Drama", "Action", "Crime", "Thriller"),
            category = "top_rated",
            trailerYoutubeId = "EXeTwQWrcwY",
            server1StreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            server2StreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4",
            cast = listOf(
                CastMember(81, "Christian Bale", "Bruce Wayne / Batman", "https://image.tmdb.org/t/p/w185/811.jpg"),
                CastMember(82, "Heath Ledger", "Joker", "https://image.tmdb.org/t/p/w185/812.jpg"),
                CastMember(83, "Gary Oldman", "Jim Gordon", "https://image.tmdb.org/t/p/w185/813.jpg")
            )
        )
    )

    val actionMovies = listOf(
        Movie(
            id = 603692,
            title = "John Wick: Chapter 4",
            overview = "With the price on his head ever increasing, John Wick uncovers a path to defeating The High Table. But before he can earn his freedom, Wick must face off against a new enemy with powerful alliances across the globe.",
            posterPath = "https://image.tmdb.org/t/p/w780/vZloFAK7NKnMGKEslUsZloiojrQ.jpg",
            backdropPath = "https://image.tmdb.org/t/p/w1280/7I6VUdPj6tQECNHdviJkUHD2f89.jpg",
            releaseDate = "2023-03-22",
            voteAverage = 7.7,
            voteCount = 6200,
            runtime = "2h 49m",
            genres = listOf("Action", "Thriller", "Crime"),
            category = "action",
            trailerYoutubeId = "qEVUtrk8_B4",
            server1StreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            server2StreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            cast = listOf(
                CastMember(91, "Keanu Reeves", "John Wick", "https://image.tmdb.org/t/p/w185/91.jpg"),
                CastMember(92, "Donnie Yen", "Caine", "https://image.tmdb.org/t/p/w185/92.jpg"),
                CastMember(93, "Bill Skarsgård", "Marquis", "https://image.tmdb.org/t/p/w185/93.jpg")
            )
        ),
        Movie(
            id = 76341,
            title = "Mad Max: Fury Road",
            overview = "An apocalyptic story set in the furthest reaches of our planet, in a stark desert landscape where humanity is broken, and most everyone is crazed fighting for the necessities of life.",
            posterPath = "https://image.tmdb.org/t/p/w780/8tZYtuWezp8JbcsvHYO0O46tFbo.jpg",
            backdropPath = "https://image.tmdb.org/t/p/w1280/nlCHUW2Y9XWbuR2nn7Y9csdpAzy.jpg",
            releaseDate = "2015-05-13",
            voteAverage = 7.6,
            voteCount = 22100,
            runtime = "2h 00m",
            genres = listOf("Action", "Adventure", "Sci-Fi"),
            category = "action",
            trailerYoutubeId = "hEJnMQG9ev8",
            server1StreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            server2StreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
            cast = listOf(
                CastMember(101, "Tom Hardy", "Max Rockatansky", "https://image.tmdb.org/t/p/w185/101.jpg"),
                CastMember(102, "Charlize Theron", "Imperator Furiosa", "https://image.tmdb.org/t/p/w185/102.jpg")
            )
        ),
        Movie(
            id = 414906,
            title = "The Batman",
            overview = "In his second year of fighting crime, Batman uncovers corruption in Gotham City that connects to his own family while facing a serial killer known as the Riddler.",
            posterPath = "https://image.tmdb.org/t/p/w780/74xTEgt7R36Fpooo50r9T25onhq.jpg",
            backdropPath = "https://image.tmdb.org/t/p/w1280/b0PlSFdDwbyK0cf5RxwDpaOJQvQ.jpg",
            releaseDate = "2022-03-01",
            voteAverage = 7.7,
            voteCount = 9500,
            runtime = "2h 56m",
            genres = listOf("Crime", "Mystery", "Thriller", "Action"),
            category = "action",
            trailerYoutubeId = "mqqft2x_Aa4",
            server1StreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            server2StreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
            cast = listOf(
                CastMember(111, "Robert Pattinson", "Bruce Wayne / Batman", "https://image.tmdb.org/t/p/w185/111.jpg"),
                CastMember(112, "Zoë Kravitz", "Selina Kyle / Catwoman", "https://image.tmdb.org/t/p/w185/112.jpg")
            )
        ),
        Movie(
            id = 27205,
            title = "Inception",
            overview = "Cobb, a skilled thief who commits corporate espionage by infiltrating the subconscious of his targets is offered a chance to regain his old life as payment for a task considered to be impossible: \"inception\".",
            posterPath = "https://image.tmdb.org/t/p/w780/oYuLEt3zVCKq57qu2F8dT7NIa6f.jpg",
            backdropPath = "https://image.tmdb.org/t/p/w1280/s3TBrRGB1iav7gFOCNx3H31MoES.jpg",
            releaseDate = "2010-07-15",
            voteAverage = 8.4,
            voteCount = 36000,
            runtime = "2h 28m",
            genres = listOf("Action", "Sci-Fi", "Adventure"),
            category = "action",
            trailerYoutubeId = "YoHD9XEInc0",
            server1StreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            server2StreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            cast = listOf(
                CastMember(121, "Leonardo DiCaprio", "Dom Cobb", "https://image.tmdb.org/t/p/w185/121.jpg"),
                CastMember(122, "Joseph Gordon-Levitt", "Arthur", "https://image.tmdb.org/t/p/w185/122.jpg")
            )
        )
    )

    val popularTvShows = listOf(
        Movie(
            id = 66732,
            title = "Stranger Things",
            overview = "When a young boy vanishes, a small town uncovers a mystery involving secret experiments, terrifying supernatural forces and one strange little girl.",
            posterPath = "https://image.tmdb.org/t/p/w780/49WJfeN0moxb9IPfGn8AIqMGskD.jpg",
            backdropPath = "https://image.tmdb.org/t/p/w1280/56v2KjBlU4XaOv9rVYEQypROD7P.jpg",
            releaseDate = "2016-07-15",
            voteAverage = 8.6,
            voteCount = 17100,
            runtime = "50m/ep",
            genres = listOf("Sci-Fi", "Drama", "Mystery"),
            category = "tv_shows",
            isTvShow = true,
            trailerYoutubeId = "b9EkMc79ZSU",
            server1StreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            server2StreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
            cast = listOf(
                CastMember(131, "Millie Bobby Brown", "Eleven", "https://image.tmdb.org/t/p/w185/131.jpg"),
                CastMember(132, "David Harbour", "Jim Hopper", "https://image.tmdb.org/t/p/w185/132.jpg"),
                CastMember(133, "Winona Ryder", "Joyce Byers", "https://image.tmdb.org/t/p/w185/133.jpg")
            )
        ),
        Movie(
            id = 100088,
            title = "The Last of Us",
            overview = "Twenty years after modern civilization has been destroyed, Joel, a hardened survivor, is hired to smuggle Ellie, a 14-year-old girl, out of an oppressive quarantine zone.",
            posterPath = "https://image.tmdb.org/t/p/w780/uKvVjHNqB5VmOrdxqAt2V7JMrRI.jpg",
            backdropPath = "https://image.tmdb.org/t/p/w1280/uDgy6hyPd82kOHh6I95FLtLnj6p.jpg",
            releaseDate = "2023-01-15",
            voteAverage = 8.6,
            voteCount = 5200,
            runtime = "55m/ep",
            genres = listOf("Drama", "Sci-Fi", "Action"),
            category = "tv_shows",
            isTvShow = true,
            trailerYoutubeId = "uLtkt8BonwM",
            server1StreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            server2StreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            cast = listOf(
                CastMember(141, "Pedro Pascal", "Joel Miller", "https://image.tmdb.org/t/p/w185/141.jpg"),
                CastMember(142, "Bella Ramsey", "Ellie Williams", "https://image.tmdb.org/t/p/w185/142.jpg")
            )
        ),
        Movie(
            id = 94605,
            title = "Arcane",
            overview = "Amid the stark discord of twin cities Piltover and Zaun, two sisters fight on rival sides of a war between magic technologies and incompatible convictions.",
            posterPath = "https://image.tmdb.org/t/p/w780/fqldf2t8ztc9aiwn397rWW2vncv.jpg",
            backdropPath = "https://image.tmdb.org/t/p/w1280/2meX1nMdScFOoV4370rqHWKm5oY.jpg",
            releaseDate = "2021-11-06",
            voteAverage = 8.7,
            voteCount = 4100,
            runtime = "42m/ep",
            genres = listOf("Animation", "Sci-Fi", "Action", "Drama"),
            category = "tv_shows",
            isTvShow = true,
            trailerYoutubeId = "fXmAurh012s",
            server1StreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            server2StreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4",
            cast = listOf(
                CastMember(151, "Hailee Steinfeld", "Vi", "https://image.tmdb.org/t/p/w185/151.jpg"),
                CastMember(152, "Ella Purnell", "Jinx", "https://image.tmdb.org/t/p/w185/152.jpg")
            )
        ),
        Movie(
            id = 1399,
            title = "Game of Thrones",
            overview = "Seven noble families fight for control of the mythical land of Westeros. Friction between the houses leads to full-scale war. All while a very ancient evil awakens in the farthest north.",
            posterPath = "https://image.tmdb.org/t/p/w780/1XS1oqL89opfnbLl8WnZY1O1uJx.jpg",
            backdropPath = "https://image.tmdb.org/t/p/w1280/2OMB0ynKlyIenMJWI2Dy9IWT4c.jpg",
            releaseDate = "2011-04-17",
            voteAverage = 8.4,
            voteCount = 23900,
            runtime = "55m/ep",
            genres = listOf("Sci-Fi & Fantasy", "Drama", "Action"),
            category = "tv_shows",
            isTvShow = true,
            trailerYoutubeId = "KPLWWIOCOOQ",
            server1StreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            server2StreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
            cast = listOf(
                CastMember(161, "Emilia Clarke", "Daenerys Targaryen", "https://image.tmdb.org/t/p/w185/161.jpg"),
                CastMember(162, "Kit Harington", "Jon Snow", "https://image.tmdb.org/t/p/w185/162.jpg"),
                CastMember(163, "Peter Dinklage", "Tyrion Lannister", "https://image.tmdb.org/t/p/w185/163.jpg")
            )
        )
    )

    val allMovies: List<Movie> by lazy {
        val list = mutableListOf<Movie>()
        list.addAll(trendingMovies)
        topRatedMovies.forEach { m -> if (list.none { it.id == m.id }) list.add(m) }
        actionMovies.forEach { m -> if (list.none { it.id == m.id }) list.add(m) }
        popularTvShows.forEach { m -> if (list.none { it.id == m.id }) list.add(m) }
        list
    }
}
