import { getSizeMixin } from './size.js'

const OptionSizeMixin = getSizeMixin({
  xs: 30,
  sm: 35,
  md: 40,
  lg: 50,
  xl: 60
})

const checkboxOptionSizeMixin = getSizeMixin({
  md: 36,
  lg: 44
});

export {
  OptionSizeMixin,
  checkboxOptionSizeMixin
}
