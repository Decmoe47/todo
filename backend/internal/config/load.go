package config

import "github.com/ilyakaznacheev/cleanenv"

func Load(path string) (*Root, error) {
	var root Root
	err := cleanenv.ReadConfig(path, &root)
	if err != nil {
		return nil, err
	}
	return &root, nil
}
