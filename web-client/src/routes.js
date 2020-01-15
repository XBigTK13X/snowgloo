import pages from './page';

export default [
    {
      name: 'home',
      url: '/',
      component: pages.Home,
    },
    {
      name: 'song-list',
      url: '/song/list',
      component: pages.SongList
    }
]
