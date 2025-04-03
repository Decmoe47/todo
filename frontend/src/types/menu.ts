export interface MenuItem {
  label: string
  action?: () => Promise<void>
  children?: MenuItems
}

export interface MenuItems {
  [key: string] : MenuItem
}
