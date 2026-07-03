package cz.cernilovsky.kmp.rickandmorty.core.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * The specific Material icons used by the app, vendored as [ImageVector]s so we don't depend on the
 * deprecated `material-icons-extended` artifact (which bundled thousands of unused vectors).
 *
 * Each icon is the classic filled "Material Icons" 24dp vector, defined from its raw SVG path data.
 * Circles in the source (e.g. [Category], [Pets]) are expressed as equivalent arc paths. The fill
 * colour is a placeholder — `Icon(...)` tints the vector with `LocalContentColor` at draw time.
 */
object AppIcons {
    val ArrowBack: ImageVector by lazy {
        materialIcon(
            name = "ArrowBack",
            autoMirror = true,
            pathData = "M20 11H7.83l5.59-5.59L12 4l-8 8l8 8l1.41-1.41L7.83 13H20z",
        )
    }

    val Category: ImageVector by lazy {
        materialIcon(
            name = "Category",
            pathData =
                "m12 2l-5.5 9h11z" +
                    "M13 17.5a4.5 4.5 0 1 0 9 0a4.5 4.5 0 1 0 -9 0z" +
                    "M3 13.5h8v8H3z",
        )
    }

    val Check: ImageVector by lazy {
        materialIcon(
            name = "Check",
            pathData = "M9 16.17L4.83 12l-1.42 1.41L9 19L21 7l-1.41-1.41z",
        )
    }

    val Close: ImageVector by lazy {
        materialIcon(
            name = "Close",
            pathData =
                "M19 6.41L17.59 5L12 10.59L6.41 5L5 6.41L10.59 12L5 17.59L6.41 19L12 " +
                    "13.41L17.59 19L19 17.59L13.41 12z",
        )
    }

    val FilterList: ImageVector by lazy {
        materialIcon(
            name = "FilterList",
            pathData = "M10 18h4v-2h-4zM3 6v2h18V6zm3 7h12v-2H6z",
        )
    }

    val LocalMovies: ImageVector by lazy {
        materialIcon(
            name = "LocalMovies",
            pathData =
                "M18 3v2h-2V3H8v2H6V3H4v18h2v-2h2v2h8v-2h2v2h2V3zM8 17H6v-2h2zm0-4H6v-2h2z" +
                    "m0-4H6V7h2zm10 8h-2v-2h2zm0-4h-2v-2h2zm0-4h-2V7h2z",
        )
    }

    val LocationOn: ImageVector by lazy {
        materialIcon(
            name = "LocationOn",
            pathData =
                "M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7m0 " +
                    "9.5a2.5 2.5 0 0 1 0-5a2.5 2.5 0 0 1 0 5",
        )
    }

    val MonitorHeart: ImageVector by lazy {
        materialIcon(
            name = "MonitorHeart",
            pathData =
                "M15.11 12.45L14 10.24l-3.11 6.21c-.16.34-.51.55-.89.55s-.73-.21-.89-.55L7." +
                    "38 13H2v5c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2v-5h-6c-.38 0-.73-.21-.89-.55" +
                    "M20 4H4c-1.1 0-2 .9-2 2v5h6c.38 0 .73.21.89.55L10 13.76l3.11-6.21c.34-.68 " +
                    "1.45-.68 1.79 0L16.62 11H22V6c0-1.1-.9-2-2-2",
        )
    }

    val Pets: ImageVector by lazy {
        materialIcon(
            name = "Pets",
            pathData =
                "M2 9.5a2.5 2.5 0 1 0 5 0a2.5 2.5 0 1 0 -5 0z" +
                    "M6.5 5.5a2.5 2.5 0 1 0 5 0a2.5 2.5 0 1 0 -5 0z" +
                    "M12.5 5.5a2.5 2.5 0 1 0 5 0a2.5 2.5 0 1 0 -5 0z" +
                    "M17 9.5a2.5 2.5 0 1 0 5 0a2.5 2.5 0 1 0 -5 0z" +
                    "M17.34 14.86c-.87-1.02-1.6-1.89-2.48-2.91c-.46-.54-1.05-1.08-1.75-1.32q-.165-.06" +
                    "-.33-.09c-.25-.04-.52-.04-.78-.04s-.53 0-.79.05q-.165.03-.33.09c-.7.24-1.28.78" +
                    "-1.75 1.32c-.87 1.02-1.6 1.89-2.48 2.91c-1.31 1.31-2.92 2.76-2.62 4.79c.29 1.02 " +
                    "1.02 2.03 2.33 2.32c.73.15 3.06-.44 5.54-.44h.18c2.48 0 4.81.58 5.54.44c1.31-.29 " +
                    "2.04-1.31 2.33-2.32c.31-2.04-1.3-3.49-2.61-4.8",
        )
    }

    val Wc: ImageVector by lazy {
        materialIcon(
            name = "Wc",
            pathData =
                "M5.5 22v-7.5H4V9c0-1.1.9-2 2-2h3c1.1 0 2 .9 2 2v5.5H9.5V22zM18 22v-6h3l-2." +
                    "54-7.63A2.01 2.01 0 0 0 16.56 7h-.12a2 2 0 0 0-1.9 1.37L12 16h3v6zM7.5 6c1.11 0 " +
                    "2-.89 2-2s-.89-2-2-2s-2 .89-2 2s.89 2 2 2m9 0c1.11 0 2-.89 2-2s-.89-2-2-2s-2 .89" +
                    "-2 2s.89 2 2 2",
        )
    }
}

private fun materialIcon(
    name: String,
    pathData: String,
    autoMirror: Boolean = false,
): ImageVector =
    ImageVector
        .Builder(
            name = "AppIcons.$name",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
            autoMirror = autoMirror,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString(pathData).toNodes(),
                fill = SolidColor(Color.Black),
            )
        }.build()
