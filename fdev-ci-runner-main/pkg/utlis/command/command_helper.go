package command

import (
	"errors"
	"fmt"
	"github.com/spf13/pflag"
	"os/exec"
	"reflect"
	"runtime"
	"strconv"
	"strings"
)

func ExecuteCommand(name string, subname string, args ...string) (string, error) {
	args = append([]string{subname}, args...)

	cmd := exec.Command(name, args...)
	bytes, err := cmd.CombinedOutput()

	return string(bytes), err
}

func ToPFlags(v interface{}, set *pflag.FlagSet) error {
	value := reflect.ValueOf(v)
	if value.Kind() == reflect.Ptr {
		value = value.Elem()
		if value.Kind() == reflect.Struct {
			for i := 0; i < value.NumField(); i++ {
				fieldValue := value.Field(i)
				field := value.Type().Field(i)
				keyName, _ := getMapStructure(field)
				shortHand := field.Tag.Get("short")
				usage := field.Tag.Get("usage")
				value := field.Tag.Get("value")
				switch field.Type.Kind() {
				case reflect.String:
					set.StringVarP(fieldValue.Addr().Interface().(*string), keyName, shortHand, value, usage)
				case reflect.Int:
					var intValue int
					if value != "" {
						parseInt, err := strconv.ParseInt(value, 10, 32)
						if err != nil {
							return err
						}
						intValue = int(parseInt)
					}
					set.IntVarP(fieldValue.Addr().Interface().(*int), keyName, shortHand, intValue, usage)
				default:
					return errors.New(fmt.Sprint("ToPFlags not support type", field.Type))
				}
			}
		} else {
			return errors.New(fmt.Sprint("ToPFlags not support type", value))
		}
	} else {
		return errors.New("ToPFlags need a pointer")
	}
	return nil
}
func ToPArgs(v interface{}) (string, error) {
	value := reflect.ValueOf(v)
	var stringFlags []string
	if value.Kind() == reflect.Struct {
		for i := 0; i < value.NumField(); i++ {
			field := value.Type().Field(i)
			fieldValue := value.Field(i)
			keyName, omitempty := getMapStructure(field)
			if fieldValue.IsZero() && omitempty {
				continue
			}
			var argValue string
			if runtime.GOOS == "windows" {
				argValue = strings.ReplaceAll(fmt.Sprint(fieldValue), "%", "%%")
			} else {
				argValue = fmt.Sprint(fieldValue)
			}
			arg := fmt.Sprintf("--%s=%s", keyName, argValue)
			stringFlags = append(stringFlags, arg)
		}
	} else {
		return "", errors.New(fmt.Sprint("ToPArgs not support value", v))
	}
	return strings.Join(stringFlags, " "), nil
}

func getMapStructure(field reflect.StructField) (keyName string, omitempty bool) {
	tagParts := strings.Split(field.Tag.Get("mapstructure"), ",")
	for _, tag := range tagParts[1:] {
		if tag == "omitempty" {
			omitempty = true
			break
		}
	}
	keyName = field.Name
	if tagParts[0] != "" && tagParts[0] != "-" {
		keyName = tagParts[0]
	}
	return
}
