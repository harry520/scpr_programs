package com.harry.scprprograms.model

data class DefaultPlaylistX(
    val ArtworkUrl: String,
    val Categories: List<String>,
    val CustomFieldData: Any,
    val Description: String,
    val DescriptionHtml: String,
    val DirectoryLinks: DirectoryLinksX,
    val EmbedUrl: String,
    val Id: String,
    val NumberOfClips: Int,
    val OrganizationId: String,
    val ProgramId: String,
    val RssFeedUrl: String,
    val Slug: String,
    val Title: String,
    val Visibility: String
)