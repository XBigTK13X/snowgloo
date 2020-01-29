import pages from './page'

export default [
    {
        name: 'admin',
        url: '/admin',
        component: pages.Admin,
    },
    {
        name: 'queue',
        url: '/',
        component: pages.Queue,
    },
    {
        name: 'artist-list',
        url: '/artist/list',
        component: pages.ArtistList,
    },
    {
        name: 'album-list',
        url: '/album/list',
        component: pages.AlbumList,
    },
    {
        name: 'artist-view',
        url: '/artist/view/:artist',
        component: pages.ArtistView,
    },
    {
        name: 'album-view',
        url: '/album/view/:albumSlug',
        component: pages.AlbumView,
    },
    {
        name: 'song-list',
        url: '/song/list',
        component: pages.SongList,
    },
    {
        name: 'search',
        url: '/search',
        component: pages.Search,
    },
]
